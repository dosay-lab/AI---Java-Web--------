import com.sun.net.httpserver.HttpServer; // Webサーバーを使うための読み込み
import java.net.InetSocketAddress; // 待ち受ける番号を指定するための読み込み

public class App { // プログラム本体の始まり
    public static void main(String[] args) throws Exception { // 実行を始める場所
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // 8080番でWebサーバーを用意
        server.createContext("/", exchange -> { // 「/」へのアクセスを受けたときの処理
            String message = "Hello, World!"; // 返す文字
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8"); // UTF-8で文字を返すと指定
            byte[] body = message.getBytes("UTF-8"); // 文字を送信用のデータに変換
            exchange.sendResponseHeaders(200, body.length); // 成功とデータの長さを通知
            exchange.getResponseBody().write(body); // データを送信
            exchange.getResponseBody().close(); // 送信を終了
        }); // 「/」への処理の終わり
        server.start(); // サーバーを起動
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // 起動メッセージを表示
    } // 実行部分の終わり
} // プログラム本体の終わり
