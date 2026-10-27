import com.sun.net.httpserver.HttpExchange; // ★
import com.sun.net.httpserver.HttpServer; // ★
import java.io.IOException; // ★
import java.net.InetSocketAddress; // ★
import java.net.URLDecoder; // ★
import java.nio.charset.StandardCharsets; // ★
import java.sql.Connection; // ★追加 SQLiteへの接続に使います。
import java.sql.DriverManager; // ★追加 JDBCでデータベースを開きます。
import java.sql.PreparedStatement; // ★追加 SQLに値を渡します。
import java.sql.ResultSet; // ★追加 SELECTの結果を読みます。
import java.sql.SQLException; // ★追加 SQLの失敗を扱います。
import java.sql.Statement; // ★追加 テーブルを作ります。
import java.util.ArrayList; // ★追加 SELECT結果を一覧にします。
import java.util.List; // ★追加 Todo一覧の型です。

public class App { // ★
    private static final String DB_URL = "jdbc:sqlite:todos.db"; // ★追加 保存先です。

    private static Connection connect() throws SQLException { // ★追加 接続を開きます。
        return DriverManager.getConnection(DB_URL); // ★追加
    } // ★

    private static void initDatabase() throws SQLException { // ★追加 起動時に表を用意します。
        try (Connection db = connect(); Statement stmt = db.createStatement()) { // ★追加
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS todos (id INTEGER PRIMARY KEY, title TEXT, done INTEGER)"); // ★追加
        } // ★
    } // ★

    private static void addTodo(String title) throws SQLException { // ★追加 Todoを追加します。
        try (Connection db = connect(); // ★
             PreparedStatement stmt = db.prepareStatement("INSERT INTO todos (title, done) VALUES (?, 0)")) { // ★追加
            stmt.setString(1, title); // ★追加
            stmt.executeUpdate(); // ★追加
        } // ★
    } // ★

    private static void completeTodo(int id) throws SQLException { // ★追加 Todoを完了にします。
        try (Connection db = connect(); // ★
             PreparedStatement stmt = db.prepareStatement("UPDATE todos SET done = 1 WHERE id = ?")) { // ★追加
            stmt.setInt(1, id); // ★追加
            stmt.executeUpdate(); // ★追加
        } // ★
    } // ★

    private static void deleteTodo(int id) throws SQLException { // ★追加 Todoを削除します。
        try (Connection db = connect(); // ★
             PreparedStatement stmt = db.prepareStatement("DELETE FROM todos WHERE id = ?")) { // ★追加
            stmt.setInt(1, id); // ★追加
            stmt.executeUpdate(); // ★追加
        } // ★
    } // ★

    private static List<Todo> listTodos() throws SQLException { // ★追加 一覧をDBから読みます。
        List<Todo> todos = new ArrayList<>(); // ★追加
        try (Connection db = connect(); // ★
             PreparedStatement stmt = db.prepareStatement("SELECT id, title, done FROM todos ORDER BY id"); // ★
             ResultSet rows = stmt.executeQuery()) { // ★追加
            while (rows.next()) { // ★追加
                todos.add(new Todo(rows.getInt("id"), rows.getString("title"), rows.getInt("done") != 0)); // ★追加
            } // ★
        } // ★
        return todos; // ★追加
    } // ★

    private static String formValue(String body, String key) { // ★追加 POSTの入力を取り出します。
        for (String field : body.split("&")) { // ★追加
            int equals = field.indexOf('='); // ★追加
            if (equals < 0) continue; // ★追加
            String name = URLDecoder.decode(field.substring(0, equals), StandardCharsets.UTF_8); // ★追加
            if (name.equals(key)) { // ★
                return URLDecoder.decode(field.substring(equals + 1), StandardCharsets.UTF_8); // ★追加
            } // ★
        } // ★
        return ""; // ★追加
    } // ★

    private static Integer queryId(String query) { // ★追加 URLのidを調べます。
        if (query == null || !query.startsWith("id=")) return null; // ★追加
        try { // ★
            return Integer.parseInt(query.substring(3)); // ★追加
        } catch (NumberFormatException e) { // ★
            return null; // ★追加
        } // ★
    } // ★

    private static String escapeHtml(String value) { // ★追加 タイトルをHTMLとして解釈させません。
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") // ★追加
                .replace("\"", "&quot;").replace("'", "&#39;"); // ★追加
    } // ★

    private static String escapeJson(String value) { // JSON文字列に必要な文字をエスケープします。
        StringBuilder escaped = new StringBuilder(); // 変換後の文字列を作ります。
        for (int i = 0; i < value.length(); i++) { // 各文字を順に調べます。
            char c = value.charAt(i); // 現在の文字を取り出します。
            switch (c) { // 特別な文字を判定します。
                case '"' -> escaped.append("\\\""); // 二重引用符をエスケープします。
                case '\\' -> escaped.append("\\\\"); // バックスラッシュをエスケープします。
                case '\b' -> escaped.append("\\b"); // バックスペースをエスケープします。
                case '\f' -> escaped.append("\\f"); // フォームフィードをエスケープします。
                case '\n' -> escaped.append("\\n"); // 改行をエスケープします。
                case '\r' -> escaped.append("\\r"); // 復帰をエスケープします。
                case '\t' -> escaped.append("\\t"); // タブをエスケープします。
                default -> { // その他の文字を処理します。
                    if (c < 0x20) { // 残りの制御文字を判定します。
                        String hex = Integer.toHexString(c); // 文字コードを16進数にします。
                        escaped.append("\\u"); // Unicodeエスケープを始めます。
                        for (int j = hex.length(); j < 4; j++) escaped.append('0'); // 4桁になるまでゼロを補います。
                        escaped.append(hex); // 文字コードを追加します。
                    } else { // 通常の文字を処理します。
                        escaped.append(c); // 文字をそのまま追加します。
                    } // 制御文字の判定を終えます。
                } // その他の文字の処理を終えます。
            } // 特別な文字の判定を終えます。
        } // 全文字の処理を終えます。
        return escaped.toString(); // 変換後の文字列を返します。
    } // JSONのエスケープ処理を終えます。

    private static void send(HttpExchange exchange, int status, String type, String message) throws IOException { // ★追加
        byte[] body = message.getBytes(StandardCharsets.UTF_8); // ★追加
        exchange.getResponseHeaders().set("Content-Type", type + "; charset=UTF-8"); // ★追加
        exchange.sendResponseHeaders(status, body.length); // ★追加
        try (java.io.OutputStream output = exchange.getResponseBody()) { // ★追加
            output.write(body); // ★追加
        } // ★
    } // ★

    private static void redirect(HttpExchange exchange) throws IOException { // ★追加
        exchange.getResponseHeaders().set("Location", "/"); // ★追加
        exchange.sendResponseHeaders(303, -1); // ★追加
        exchange.close(); // ★追加
    } // ★

    public static void main(String[] args) throws Exception { // ★
        initDatabase(); // ★変更 CSVの読み込みに代えて表を用意します。
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0); // ★
        server.createContext("/api/todos", exchange -> { // Todo一覧のAPIを登録します。
            if (!exchange.getRequestURI().getPath().equals("/api/todos")) { // 別のパスへのアクセスを除外します。
                exchange.sendResponseHeaders(404, -1); // 404を返します。
                exchange.close(); // 通信を閉じます。
                return; // 処理を終えます。
            } // パスの確認を終えます。
            if (!exchange.getRequestMethod().equals("GET")) { // GET以外を除外します。
                exchange.getResponseHeaders().set("Allow", "GET"); // 利用できるメソッドを示します。
                exchange.sendResponseHeaders(405, -1); // 405を返します。
                exchange.close(); // 通信を閉じます。
                return; // 処理を終えます。
            } // メソッドの確認を終えます。
            try { // データベースのエラーを処理します。
                StringBuilder json = new StringBuilder("["); // JSON配列を作り始めます。
                for (Todo todo : listTodos()) { // 全Todoを順に読み込みます。
                    if (json.length() > 1) json.append(','); // 2件目以降の前に区切りを入れます。
                    json.append("{\"title\":\"").append(escapeJson(todo.title)).append("\",\"done\":").append(todo.done).append('}'); // タイトルと完了状態を追加します。
                } // 全Todoの追加を終えます。
                json.append(']'); // JSON配列を閉じます。
                byte[] body = json.toString().getBytes(StandardCharsets.UTF_8); // 応答をUTF-8のバイト列にします。
                exchange.getResponseHeaders().set("Content-Type", "application/json"); // charsetを付けずに種類を指定します。
                exchange.sendResponseHeaders(200, body.length); // 応答の状態と長さを送ります。
                try (java.io.OutputStream output = exchange.getResponseBody()) { // 応答の出力先を開きます。
                    output.write(body); // JSONを送ります。
                } // 出力先を閉じます。
            } catch (SQLException e) { // データベースのエラーを受け取ります。
                e.printStackTrace(); // エラーをサーバー側に記録します。
                exchange.sendResponseHeaders(500, -1); // 500を返します。
                exchange.close(); // 通信を閉じます。
            } // エラー処理を終えます。
        }); // APIの登録を終えます。
        server.createContext("/", exchange -> { // ★
            String path = exchange.getRequestURI().getPath(); // ★
            String method = exchange.getRequestMethod(); // ★
            try { // ★追加 SQLエラーをHTTP 500で返します。
                if (path.equals("/add") && method.equals("POST")) { // ★
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); // ★
                    String title = formValue(body, "todo"); // ★変更 期限日は表にないため読みません。
                    if (!title.isEmpty()) addTodo(title); // ★変更 INSERTで保存します。
                    redirect(exchange); // ★変更
                    return; // ★
                } else if (path.equals("/done") && method.equals("GET")) { // ★
                    Integer id = queryId(exchange.getRequestURI().getQuery()); // ★変更
                    if (id != null) completeTodo(id); // ★変更 UPDATEで保存します。
                    redirect(exchange); // ★変更
                    return; // ★
                } else if (path.equals("/delete") && method.equals("GET")) { // ★
                    Integer id = queryId(exchange.getRequestURI().getQuery()); // ★変更
                    if (id != null) deleteTodo(id); // ★変更 DELETEで保存します。
                    redirect(exchange); // ★変更
                    return; // ★
                } else if (path.equals("/") && method.equals("GET")) { // ★
                    List<Todo> todos = listTodos(); // ★変更 SELECTで最新の一覧を読みます。
                    StringBuilder html = new StringBuilder("<!doctype html><html lang='ja'><head><meta charset='UTF-8'><title>わたしのTodo</title>" // ★
                            + "<style>body{max-width:40rem;margin:2rem auto;padding:0 1rem;font-size:1rem}h1{font-size:1.5rem}</style>" // ★
                            + "</head><body><h1>わたしのTodo</h1>" // ★
                            + "<form method='post' action='/add'><input name='todo'><button>追加</button></form>"); // ★変更 期限日欄を外します。
                    if (todos.isEmpty()) html.append("<p>やることは、いまゼロです</p>"); // ★変更
                    html.append("<ul>"); // ★変更
                    for (Todo todo : todos) { // ★
                        String mark = todo.done ? " ✔" : ""; // ★変更
                        html.append("<li>").append(escapeHtml(todo.title)).append(mark) // ★変更
                                .append(" <a href='/done?id=").append(todo.id) // ★
                                .append("'>完了</a> <a href='/delete?id=").append(todo.id) // ★
                                .append("'>削除</a></li>"); // ★変更
                    } // ★
                    html.append("</ul></body></html>"); // ★変更
                    send(exchange, 200, "text/html", html.toString()); // ★変更
                } else { // ★
                    send(exchange, 404, "text/plain", "ページが見つかりません"); // ★変更
                } // ★
            } catch (SQLException e) { // ★追加
                e.printStackTrace(); // ★追加 サーバー側で原因を確認できます。
                send(exchange, 500, "text/plain", "データベースエラー"); // ★追加
            } // ★
        }); // ★
        server.start(); // ★
        System.out.println("サーバー起動: http://localhost:8080 （止めるときは Ctrl+C）"); // ★
    } // ★
} // ★

class Todo { // ★変更 DBから読んだ1件を表します。
    final int id; // ★変更
    final String title; // ★変更
    final boolean done; // ★変更

    Todo(int id, String title, boolean done) { // ★変更
        this.id = id; // ★変更
        this.title = title; // ★変更
        this.done = done; // ★変更
    } // ★
} // ★
