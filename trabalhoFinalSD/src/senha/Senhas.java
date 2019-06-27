package senha;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import usuarios.Users;

/**
 *
 * @author renan
 */
public class Senhas {
    
    public boolean verificaUsers(String user, String password) throws Exception
    {
        FileReader fileReader = new FileReader(Users.path + "users.txt");
        
        try (BufferedReader bufferedReader = new BufferedReader(fileReader))
        {
            String linha = null;
            password = getMd5(password);
            List<Users> usuarios = new ArrayList<>();
            while ((linha = bufferedReader.readLine()) != null) 
            {
                int tamanho = linha.length();
                String[] newAtributo = new String[tamanho];
                newAtributo = linha.split("\\s+");
                if (newAtributo[0].equals(user) && newAtributo[1].equals(password))
                {
                    return true;
                }
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return false;
    }
    
    public String getMd5(String s) throws Exception
    {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(s.getBytes(),0,s.length());
        return new BigInteger(1,m.digest()).toString(16);
    }
    
}