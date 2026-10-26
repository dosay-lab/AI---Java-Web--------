import com.sun.net.httpserver.HttpServer; // Webサーバーを使うための読み込み【1】
import java.net.InetSocketAddress; // 待ち受ける番号を指定するための読み込み【】
import java.net.URLDecoder; // URLの文字を元に戻すための読み込み
import java.nio.charset.StandardCharsets; // UTF-8を指定するための読み込み
import java.util.List; // Todoの一覧を使うための読み込み
import java.util.ArrayList; // Todoの一覧を作るための読み込み

public class App { // プログラム本体の始まり【毎】
    static List<Todo> todos = new ArrayList<>(); // ★変更
    static int nextId = 1; // ★変更

    public static void main(String[] args) throws Exception { // 実行を始める場所【毎】
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番でWebサーバーを用意【毎】
        todos.add(new Todo(nextId++, "牛乳を買う")); // ★変更
        Todo egg = new Todo(nextId++, "卵を買う"); // ★変更
        egg.setDone(true); // ★変更
        todos.add(egg); // ★変更
        server.createContext("/", exchange -> { // 「/」へのアクセスを受けたときの処理【毎】
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出す
            String method = exchange.getRequestMethod(); // 【追加】GETかPOSTかを取り出す
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 通常は文字として返す
            if (path.equals("/add") && method.equals("POST")) { // 【追加】フォームから送られたTodoを受け取る
                // 【追加】送られた文字をUTF-8で読み取る
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                // 【追加】先頭の「todo=」を除く
                String value = body.substring(5);
                // 【追加】日本語を元に戻す
                String title = URLDecoder.decode(value, StandardCharsets.UTF_8); // ★変更
                if (!title.isEmpty()) { // ★変更
                    todos.add(new Todo(nextId, title)); // ★変更
                    nextId++; // ★変更
                }
                // 【追加】追加後は「/」に戻す
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return; // 【追加】ここで処理を終える
            } else if (path.equals("/done") && method.equals("GET")) { // ★追加
                String query = exchange.getRequestURI().getQuery(); // ★追加
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加
                    try { // ★追加
                        int id = Integer.parseInt(query.substring(3)); // ★追加
                        for (Todo todo : todos) { // ★追加
                            if (todo.getId() == id) { // ★追加
                                todo.setDone(true); // ★追加
                                break; // ★追加
                            } // ★追加
                        } // ★追加
                    } catch (NumberFormatException e) { // ★追加
                        // 何も変えない ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加
                exchange.sendResponseHeaders(303, -1); // ★追加
                exchange.close(); // ★追加
                return; // ★追加
            } else if (path.equals("/undone") && method.equals("GET")) {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.startsWith("id=") && query.length() > 3) {
                    try {
                        int id = Integer.parseInt(query.substring(3));
                        for (Todo todo : todos) {
                            if (todo.getId() == id) {
                                todo.setDone(false);
                                break;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // 何も変えない
                    }
                }
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(303, -1);
                exchange.close();
                return;
            } else if (path.equals("/delete") && method.equals("GET")) { // ★追加
                String query = exchange.getRequestURI().getQuery(); // ★追加
                if (query != null && query.startsWith("id=") && query.length() > 3) { // ★追加
                    try { // ★追加
                        int id = Integer.parseInt(query.substring(3)); // ★追加
                        todos.removeIf(todo -> todo.getId() == id); // ★追加
                    } catch (NumberFormatException e) { // ★追加
                        // 何も変えない ★追加
                    } // ★追加
                } // ★追加
                exchange.getResponseHeaders().set("Location", "/"); // ★追加
                exchange.sendResponseHeaders(303, -1); // ★追加
                exchange.close(); // ★追加
                return; // ★追加
            } else if (path.equals("/")) { // 【追加】フォームとTodo一覧を表示する
                String html = "<!DOCTYPE html><html lang='ja'><head><meta charset='UTF-8'><title>わたしのTodo</title>"
                        + "<style>body { max-width: 600px; margin: 24px auto; padding: 0 16px; font-size: 16px; } input, button { font-size: 1em; }</style>"
                        + "</head><body><h1>わたしのTodo</h1>"
                        + "<form method='post' action='/add'><input name='todo'><button>追加</button></form><ul>";
                if (todos.isEmpty()) {
                    html += "<li>やることは、いまゼロです</li>";
                }
                for (Todo todo : todos) { // ★変更
                    String title = todo.getTitle(); // 完了した項目の表示を切り替える
                    if (todo.isDone()) { // ★変更
                        title = "<s>" + title + "</s>"; // 完了した項目名に取り消し線を付ける
                    } // ★変更
                    html += "<li>" + title + " <a href='/done?id=" + todo.getId()
                            + "'>完了</a> <a href='/undone?id=" + todo.getId()
                            + "'>未完了</a> <a href='/delete?id=" + todo.getId() + "'>削除</a></li>";
                } // Todoをすべて取り出し終える
                html += "</ul></body></html>"; // 一覧とページのHTMLを閉じる
                message = html; // 組み立てたHTMLを応答にする*******
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // このパスだけHTMLとして返す
            } else {
                message = "ページが見つかりません";
            }
            byte[] body = message.getBytes("UTF-8"); // 文字を送信用のデータに変換【毎】
            exchange.sendResponseHeaders(200, body.length); // 成功とデータの長さを通知【毎】
            exchange.getResponseBody().write(body); // データを送信【毎】
            exchange.getResponseBody().close(); // 送信を終了【毎】
        }); // 「/」への処理の終わり【毎】
        server.start(); // サーバーを起動【毎】
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動メッセージを表示【毎】
    } // 実行部分の終わり【毎】
} // プログラム本体の終わり【毎】

class Todo { // ★変更
    private final int id; // ★変更
    private final String title; // ★変更
    private boolean done; // ★変更

    Todo(int id, String title) { // ★変更
        this.id = id; // ★変更
        this.title = title; // ★変更
        this.done = false; // ★変更
    } // ★変更

    int getId() { // ★変更
        return id; // ★変更
    } // ★変更

    String getTitle() { // ★変更
        return title; // ★変更
    } // ★変更

    boolean isDone() { // ★変更
        return done; // ★変更
    } // ★変更

    void setDone(boolean done) { // ★変更
        this.done = done; // ★変更
    } // ★変更
} // ★変更
