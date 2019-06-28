package usuarios;

/**
 *
 * @author renan
 */
public class Users {
    private static String nome;
    private static String pathAtual;
    public static String path = "/home/renan/trabDrive/";

    public static String getPathAtual() {
        return pathAtual;
    }

    public static void setPathAtual(String pathAtual) {
        Users.pathAtual = pathAtual;
    }

    public static String getNome() {
        return nome;
    }

    public static void setNome(String nome) {
        Users.nome = nome;
    }
    
}
