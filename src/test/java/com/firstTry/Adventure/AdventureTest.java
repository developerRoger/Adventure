package com.firstTry.Adventure;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.firstTry.Adventure.entity.UserTest;

public class AdventureTest {

	public static void main(String[] args) {
//		int i=1;
//		i++;
//		System.out.println(i);
/*		 SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	         System.out.println(simpleDateFormat.format(new Date()));*/
//	         System.out.println(new Date().getTime()/1000+15-new Date().getTime()/1000);
			 ConcurrentHashMap<String,Object> map=new ConcurrentHashMap<>();
	         map.put("1111","22222");
		         map.remove("666");
		/*	UserTest o=new UserTest();
			o.setId(1l);
			o.setMobie("111111111111");
			o.setRemark("1");
			AdventureTest adventureTest=new AdventureTest();
			Map<String,Object> sa=object2Map(o);
			System.out.println(sa.get("id"));*/
		
//		System.out.println(new BCryptPasswordEncoder().encode("123"));
	}

	private ServerSocket ss;

	private Socket socket;

	private BufferedReader in;

	private PrintWriter out;
	/**
     * 实体对象转成Map
     * @param obj 实体对象
     * @return
     */
    public static Map<String, Object> object2Map(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) {
            return map;
        }
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
	public AdventureTest()

	{
		try

		{

			ss = new ServerSocket(1709);

			while (true)

			{

				socket = ss.accept();
				System.err.println(socket.getPort() + "laile");

				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				BufferedInputStream bf = new BufferedInputStream(socket.getInputStream());

				DataInputStream dis = new DataInputStream(bf);

				byte[] bytes = new byte[1024];
				while (dis.read() != -1) {

				}
				out = new PrintWriter(socket.getOutputStream(), true);

				String line = in.readLine();
				System.out.println(line);

				out.println("you input is :" + line);

				out.close();

				in.close();

				socket.close();

			}

		}

		catch (IOException e) {
		}
		try {
			ss.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/*
	 * public static void main(String[] args) { new AdventureTest(); }
	 */
}
