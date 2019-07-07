package senha;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import usuarios.Users;

/**
 *
 * @author renan
 */
public class Senhas {

    //Cara, isso aqui só verifica se tem algum usuário compativel no arquivo txt
    public boolean verificaUsers(String user, String password) throws Exception {
        FileReader fileReader = new FileReader(Users.path + "users.txt");

        try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String linha = null;
            password = getMd5(password);
            while ((linha = bufferedReader.readLine()) != null) {
                int tamanho = linha.length();
                String[] newAtributo = new String[tamanho];
                newAtributo = linha.split("\\s+");
                if (newAtributo[0].equals(user) && newAtributo[1].equals(password)) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    // Isso aqui faz um hash simples MD5
    public String getMd5(String s) throws Exception {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(s.getBytes(), 0, s.length());
        return new BigInteger(1, m.digest()).toString(16);
    }

    public String criaNewUser(String userName, String pass) throws IOException, Exception {
        FileWriter fw = new FileWriter(Users.path + "users.txt", true);
        BufferedWriter con = new BufferedWriter(fw);
        // Criptografa a Senha com um Hash MD5 padrão
        String s = userName + " " + getMd5(pass);
        con.write(s);
        con.newLine();
        con.close();
        // Escreve tudo o que tinha pra escrever no users.txt
        // Cria um diretório para o usuário e loga ele
        new File(Users.path + userName + "/").mkdirs();
        File shared = new File(Users.path + userName + ".txt");
        if(!shared.createNewFile())
            System.out.println("Não foi possível criar o arquivo");
        return (Users.path + userName + "/");
    }
    
    public static boolean compartilhar (String comquem, String arq) throws IOException{
        FileWriter fw = new FileWriter(Users.path + comquem + ".txt", true);
        BufferedWriter con = new BufferedWriter(fw);
        String arquivo = arq;
        con.write(arquivo);
        con.newLine();
        con.close();
        return true;
    }
    
    public static String getCompartilhados(String quem) throws IOException{
        FileReader fileReader = new FileReader(Users.path + quem + ".txt");
        try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String linha = null;
            String valor = "";
            while ((linha = bufferedReader.readLine()) != null) {
                valor = linha + "\n";
            }
            return valor;
        } catch (Exception e) {
            System.out.println(e);
        }
        return "";
    }
}
