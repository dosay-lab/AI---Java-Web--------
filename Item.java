// Itemという名前のクラス（プログラムをまとめる箱）を作ります。
public class Item {
    // mainメソッド（実行を始める場所）を作ります。
    public static void main(String[] args) {
        // String型（文字を入れる種類）の変数titleに「牛乳を買う」を入れます。
        String title = "牛乳を買う";
        // +で文字をつなぎ、HTMLの1行を作ります。
        String html = "<li>" + title + "</li>";
        // 作った1行をターミナル（文字を表示する画面）に出します。
        System.out.println(html);
    // mainメソッドを閉じます。
    }
    // Itemクラスを閉じます。
}