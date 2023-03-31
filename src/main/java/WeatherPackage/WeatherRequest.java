package WeatherPackage;

import WeatherPackage.GetCord.GetCoords;
import WeatherPackage.JSONFolder.Root;
import WeatherPackage.com.company.MyRequests;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class WeatherRequest {

    public static void DeleteFile(String name) {
        File file = new File(name);
        if(file.delete()){
            System.out.println("File deleted");
        }
    }


    public static void main(String[] args) throws IOException {
        String TEMP="PUSH TO GIT";
        DeleteFile("output.txt");
        ArrayList<String> CityBuff= GetCityList("input.txt");;
        for (int i=0;i<CityBuff.size();i++) {
            MyRequests temp = new MyRequests();
            Gson gson = new Gson();
            String buffer = temp.sendGET(CityBuff.get(i));
            GetCoords data = gson.fromJson(buffer, GetCoords.class);
            String buffer2 = temp.sendGET(data.lat, data.lon);
            Root weather = gson.fromJson(buffer2, Root.class);
            ConsoleOut(weather);
            FileOut(weather);
        }
    }
    public static String GetWeather(String City) throws IOException {
            MyRequests temp = new MyRequests();
            Gson gson = new Gson();
            String buffer = temp.sendGET(City);
            if(buffer.isEmpty()){
                return "Населений пункт не знайдено.\nПеревірте правильність введеня та задайте пункт знову.\n/setmaincity";
            }
            GetCoords data = gson.fromJson(buffer, GetCoords.class);
            String buffer2 = temp.sendGET(data.lat, data.lon);
            Root weather = gson.fromJson(buffer2, Root.class);
//            ConsoleOut(weather);
//            FileOut(weather);
        String text="\nCity: "+weather.city.name+"\n";
        for (int j=0;j<weather.list.size()/5;j++){
            text+=("Temperature:"+String.valueOf((Math.round((weather.list.get(j).main.temp_max-273.15)*100.0)/100.0))+" C");
            text+=("\tWind: "+String.valueOf((Math.round((weather.list.get(j).wind.speed)*100.0)/100.0))+"m/s");
            text+=("\tDate:"+weather.list.get(j).dt_txt+"\n");
        }
        return text;
        }

    public static ArrayList<String> GetCityList(String fileName) throws IOException {
        FileReader readedfile=new FileReader(fileName);
        Scanner scaned=new Scanner(readedfile);
        ArrayList<String> Buffer=new ArrayList<String>();
        while(scaned.hasNextLine()){
            Buffer.add(scaned.nextLine());
        }
        readedfile.close();
        return Buffer;
    }

    public static void ConsoleOut(Root weather){
        System.out.println(weather.city.name);
        for (int j=0;j<weather.list.size();j++){
            System.out.print("Temperature:"+String.valueOf((Math.round((weather.list.get(j).main.temp_max-273.15)*100.0)/100.0))+" C");
            System.out.print("\tWind: "+String.valueOf((Math.round((weather.list.get(j).wind.speed)*100.0)/100.0))+"m/s");
            System.out.print("\tDate:"+weather.list.get(j).dt_txt+"\n");
        }
    }

    public static void FileOut(Root weather) throws IOException {
        FileWriter file=new FileWriter("output.txt",true);
        file.write("\nCity: "+weather.city.name+"\n");
        for (int j=0;j<weather.list.size();j++){
            file.write("Temperature: "+String.valueOf((Math.round((weather.list.get(j).main.temp_max-273.15)*100.0)/100.0))+"°C");
            file.write("\tWind: "+String.valueOf((Math.round((weather.list.get(j).wind.speed)*100.0)/100.0))+"m/s");
            file.write("\tDate:"+weather.list.get(j).dt_txt+"\n");
        }
        file.close();
    }
}