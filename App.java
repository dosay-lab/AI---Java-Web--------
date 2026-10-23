import com.sun.net.httpserver.HttpServer; // Webサーバーを使うための読み込み【1】
import java.net.InetSocketAddress; // 待ち受ける番号を指定するための読み込み【】

public class App { // プログラム本体の始まり【毎】
    public static void main(String[] args) throws Exception { // 実行を始める場所【毎】
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番でWebサーバーを用意【毎】
        server.createContext("/", exchange -> { // 「/」へのアクセスを受けたときの処理【毎】
            String path = exchange.getRequestURI().getPath(); // アクセスされたパスを取り出す
            String message;
            if (path.equals("/hello")) { // パスが「/hello」か比べる
                message = "こんにちは！";
            } else if (path.equals("/bye")) { // パスが「/bye」か比べる
                message = "さようなら！";
            } else if (path.equals("/kill")) { // パスが「/kill」か比べる
                message = "殺すよ！";
            } else {
                message = "ページが見つかりません";
            }
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // UTF-8で文字を返すと指定【毎】
            byte[] body = message.getBytes("UTF-8"); // 文字を送信用のデータに変換【毎】
            exchange.sendResponseHeaders(200, body.length); // 成功とデータの長さを通知【毎】
            exchange.getResponseBody().write(body); // データを送信【毎】
            exchange.getResponseBody().close(); // 送信を終了【毎】
        }); // 「/」への処理の終わり【毎】
        server.start(); // サーバーを起動【毎】
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動メッセージを表示【毎】
    } // 実行部分の終わり【毎】
} // プログラム本体の終わり【毎】
