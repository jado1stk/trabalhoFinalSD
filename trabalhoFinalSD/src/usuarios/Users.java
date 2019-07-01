package usuarios;

/**
 *
 * @author renan
 */
public class Users {
    private static String nome;
    private static String pathAtual;
    public static String path = "C:\\Users\\renan\\OneDrive\\Documentos\\trabDrive\\";
    // Minha do Windows = C:\\Users\\renan\\OneDrive\\Documentos\\trabDrive\\
    // Minha do Linux = /home/renan/trabDrive/
    // Altere essa variável para o seu path padrão
    // (onde vão ficar os arquivos do server)

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
