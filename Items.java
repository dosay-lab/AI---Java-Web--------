public class Items { // Itemsという名前のクラス（コードをまとめる箱）を作ります。
    public static void main(String[] args) { // プログラムをここから始めます。
        String[] todos = { "牛乳を買う", "", "パンを買う", "掃除をする" }; // Todoを配列（順番に並べた入れ物）に入れます。
        boolean[] done = { true, true, false, false }; // 同じ位置のTodoが済んだかどうかを記録します。
        for (int i = 0; i < todos.length; i++) { // iを0から始め、配列の件数まで1ずつ増やします。
            if (!todos[i].isEmpty()) { // Todoが空の文字列でない場合だけ、次の行を実行します。
                String mark = done[i] ? "[済] " : ""; // 済んだTodoだけに付ける印を決めます。
                System.out.println("<li>" + mark + todos[i] + "</li>"); // 印とTodoをタグで囲み、1行に出力します。
            } // if文（条件による分岐）の終わりです。
        } // for文の終わりです。
    } // mainの終わりです。
} // Itemsクラスの終わりです。
