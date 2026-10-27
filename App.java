import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection; // ★ DB接続
import java.sql.DriverManager; // ★ JDBC接続
import java.sql.PreparedStatement; // ★ SQLのパラメータ
import java.sql.ResultSet; // ★ SELECTの結果
import java.sql.SQLException; // ★ DBエラー
import java.sql.Statement; // ★ 表の作成
import java.util.ArrayList;
import java.util.List;

public class App {
    private static final String DB_URL = "jdbc:sqlite:todos.db"; // ★ SQLiteの保存先

    private static Connection connect() throws SQLException { // ★ 接続を開く
        return DriverManager.getConnection(DB_URL); // ★ todos.dbへ接続
    }

    private static void createTable() throws SQLException { // ★ 表を作成
        try (Connection con = connect(); Statement stmt = con.createStatement()) { // ★ SQLを実行
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS todos (id INTEGER PRIMARY KEY, title TEXT, done INTEGER)"); // ★
                                                                                                                       // 表定義
        }
    }

    private static void add(String title) throws SQLException { // ★ 追加
        try (Connection con = connect();
                PreparedStatement stmt = con.prepareStatement("INSERT INTO todos (title, done) VALUES (?, 0)")) { // ★
                                                                                                                  // INSERT
            stmt.setString(1, title); // ★ 題名を設定
            stmt.executeUpdate(); // ★ 追加を実行
        }
    }

    private static void setDone(int id, boolean done) throws SQLException { // ★ 完了状態を変更
        try (Connection con = connect();
                PreparedStatement stmt = con.prepareStatement("UPDATE todos SET done = ? WHERE id = ?")) { // ★ UPDATE
            stmt.setInt(1, done ? 1 : 0); // ★ 完了状態を設定
            stmt.setInt(2, id); // ★ 対象IDを設定
            stmt.executeUpdate(); // ★ 更新を実行
        }
    }

    private static void delete(int id) throws SQLException { // ★ 削除
        try (Connection con = connect();
                PreparedStatement stmt = con.prepareStatement("DELETE FROM todos WHERE id = ?")) { // ★ DELETE
            stmt.setInt(1, id); // ★ 対象IDを設定
            stmt.executeUpdate(); // ★ 削除を実行
        }
    }

    private static List<Todo> list() throws SQLException { // ★ 一覧を取得
        List<Todo> todos = new ArrayList<>(); // ★ SELECT結果を入れる
        try (Connection con = connect();
                PreparedStatement stmt = con.prepareStatement("SELECT id, title, done FROM todos ORDER BY id");
                ResultSet rs = stmt.executeQuery()) { // ★ SELECT
            while (rs.next()) { // ★ 1行ずつ読む
                Todo todo = new Todo(rs.getInt("id"), rs.getString("title")); // ★ IDと題名を復元
                todo.setDone(rs.getInt("done") != 0); // ★ 完了状態を復元
                todos.add(todo); // ★ 一覧へ追加
            }
        }
        return todos; // ★ 一覧を返す
    }

    private static String escapeHtml(String text) { // ★ 題名を安全に表示
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;"); // ★ HTML文字を置換
    }

    public static void main(String[] args) throws Exception {
        createTable(); // ★ 起動時に表を用意
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            try { // ★ DBエラーをHTTP応答へ変換
                if (path.equals("/add") && method.equals("POST")) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    String title = URLDecoder.decode(body.substring(5), StandardCharsets.UTF_8);
                    if (!title.isEmpty())
                        add(title); // ★ INSERTを呼ぶ
                    exchange.getResponseHeaders().set("Location", "/");
                    exchange.sendResponseHeaders(303, -1);
                    return;
                }
                if ((path.equals("/done") || path.equals("/undone") || path.equals("/delete"))
                        && method.equals("GET")) { // ★ DBの対象行を変更
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null && query.startsWith("id=") && query.length() > 3) {
                        try {
                            int id = Integer.parseInt(query.substring(3));
                            if (path.equals("/delete"))
                                delete(id); // ★ DELETEを呼ぶ
                            else
                                setDone(id, path.equals("/done")); // ★ UPDATEを呼ぶ
                        } catch (NumberFormatException ignored) {
                            // 不正なIDでは変更しない
                        }
                    }
                    exchange.getResponseHeaders().set("Location", "/");
                    exchange.sendResponseHeaders(303, -1);
                    return;
                }
                if (path.equals("/")) {
                    List<Todo> todos = list(); // ★ SELECTで一覧を読む
                    String html = "<!DOCTYPE html><html lang='ja'><head><meta charset='UTF-8'><title>わたしのTodo</title>"
                            + "<style>body { max-width: 600px; margin: 24px auto; padding: 0 16px; font-size: 16px; } input, button { font-size: 1em; }</style>"
                            + "</head><body><h1>わたしのTodo</h1>"
                            + "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>";
                    if (todos.isEmpty())
                        html += "<li>やることは、いまゼロです</li>";
                    for (Todo todo : todos) {
                        String title = escapeHtml(todo.getTitle()); // ★ DBの題名をHTML向けに変換
                        if (todo.isDone())
                            title = "<s>" + title + "</s>";
                        html += "<li>" + title + " <a href='/done?id=" + todo.getId()
                                + "'>完了</a> <a href='/undone?id=" + todo.getId()
                                + "'>未完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>";
                    }
                    html += "</ul></body></html>";
                    byte[] response = html.getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.length);
                    exchange.getResponseBody().write(response);
                } else {
                    byte[] response = "ページが見つかりません".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    exchange.sendResponseHeaders(404, response.length);
                    exchange.getResponseBody().write(response);
                }
            } catch (SQLException e) { // ★ DBエラーを通知
                e.printStackTrace(); // ★ 詳細はサーバー側に表示
                byte[] response = "データベースエラー".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                exchange.sendResponseHeaders(500, response.length);
                exchange.getResponseBody().write(response);
            } finally {
                exchange.close();
            }
        });
        server.start();
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）");
    }
}

class Todo {
    private final int id;
    private final String title;
    private boolean done;

    Todo(int id, String title) {
        this.id = id;
        this.title = title;
    }

    int getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    boolean isDone() {
        return done;
    }

    void setDone(boolean done) {
        this.done = done;
    }
}
