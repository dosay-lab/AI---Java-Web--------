import java.util.ArrayList; // ArrayList（データを複数入れる箱）を使う
import java.util.List; // List（データを順番に並べる型）を使う

class Todo { // Todo（やること1件）を表すクラス（データと処理のまとまり）
    String title; // やることの名前を保存する
    boolean done; // 済んだかどうかを保存する

    Todo(String title, boolean done) { // 新しいTodoを作るときに呼ばれる
        this.title = title; // 受け取った名前を保存する
        this.done = done; // 受け取った済み状態を保存する
    } // Todoを作る処理の終わり

    String toItem() { // 表示用の1行を作るメソッド（処理）
        if (done) { // 済んでいる場合
            return "<li>[済] " + title + "</li>"; // 「[済]」を付けて返す
        } // 済んでいる場合の終わり
        return "<li>" + title + "</li>"; // 済んでいない場合は名前だけを返す
    } // toItemの終わり
} // Todoクラスの終わり

public class Main { // Main（プログラムの入り口）を表すクラス
    public static void main(String[] args) { // 実行すると最初に呼ばれる処理
        List<Todo> todos = new ArrayList<>(); // Todoを順番に入れる箱を作る
        todos.add(new Todo("牛乳を買う", false)); // 未完了のTodoを入れる
        todos.add(new Todo("ゴミを出す", true)); // 完了したTodoを入れる
        todos.add(new Todo("全部出す", false));

        for (Todo todo : todos) { // 箱の中のTodoを1件ずつ取り出す
            System.out.println(todo.toItem()); // 作った1行をターミナルに表示する
        } // 繰り返しの終わり
    } // mainの終わり
} // Mainクラスの終わり
