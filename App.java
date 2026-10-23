import com.sun.net.httpserver.HttpServer; // Webサーバーを使うための読み込み【1】
import java.net.InetSocketAddress; // 待ち受ける番号を指定するための読み込み【】
import java.net.URLDecoder; // URLの文字を元に戻すための読み込み
import java.nio.charset.StandardCharsets; // UTF-8を指定するための読み込み
import java.util.List; // Todoの一覧を使うための読み込み
import java.util.ArrayList; // Todoの一覧を作るための読み込み

public class App { // プログラム本体の始まり【毎】
    public static void main(String[] args) throws Exception { // 実行を始める場所【毎】
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番でWebサーバーを用意【毎】
        server.createContext("/", exchange -> { // 「/」へのアクセスを受けたときの処理【毎】
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出す
            String message;
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // 通常は文字として返す
            if (path.equals("/hello")) { // パスが「/hello」か比べる
                String query = exchange.getRequestURI().getRawQuery(); // URLのクエリを取り出す【追加】
                System.out.println("query = " + query);
                String name = query == null ? "ゲスト" : query.substring(5); // name が無ければゲストにする
                name = URLDecoder.decode(name, StandardCharsets.UTF_8); // %で表された文字を元に戻す
                message = "こんにちは、" + name + "さん！"; // 名前を応答に混ぜる【変更】
            } else if (path.equals("/todos")) { // Todo一覧のパスを調べる
                List<String> todos = new ArrayList<>(); // Todoの文字列を入れる一覧を作る
                todos.add("牛乳を買う"); // 1件目を入れる
                todos.add("卵を買う"); // 2件目を入れる
                todos.add("パンを買う"); // 3件目を入れる
                todos.add("自分の今日のTodo");
                String html = "<ul>"; // 一覧のHTMLを始める
                for (String todo : todos) { // Todoを1件ずつ取り出す
                    html += "<li>" + todo + "</li>"; // 1件をHTMLに足す
                } // Todoをすべて取り出し終える
                html += "</ul>"; // 一覧のHTMLを閉じる
                message = html; // 組み立てたHTMLを応答にする*******
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); // このパスだけHTMLとして返す
            } else if (path.equals("/bye")) { // パスが「/bye」か比べる
                message = "さようなら！";
            } else if (path.equals("/kill")) { // パスが「/kill」か比べる
                message = "殺すよ！";
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
