
import java.util.*;

public class Main {
    static class FarmDataManager {
        static FarmDataManager obj = new FarmDataManager();
        private FarmDataManager() {}
    }

    interface Sensor { double read(); }

    static class SoilSensor implements Sensor {
        public double read() { return 30; }
    }

    static class TempSensor implements Sensor {
        public double read() { return 28; }
    }

    static class SensorFactory {
        static Sensor create(String s) {
            if(s.equals("soil")) return new SoilSensor();
            if(s.equals("temp")) return new TempSensor();
            throw new IllegalArgumentException();
        }
    }

    interface Observer { void update(String s); }

    static class Farmer implements Observer {
        public void update(String s) { System.out.println(s); }
    }

    static class Dashboard implements Observer {
        public void update(String s) { System.out.println(s); }
    }

    static class Publisher {
        List<Observer> list = new ArrayList<>();
        void add(Observer o) { list.add(o); }
        void remove(Observer o) { list.remove(o); }
        void notifyAll(String s) {
            for(Observer o:list) o.update(s);
        }
    }

    interface Strategy { boolean check(double m, boolean rain); }

    static class Normal implements Strategy {
        public boolean check(double m, boolean r) {
            return m<40 && !r;
        }
    }

    static class Saving implements Strategy {
        public boolean check(double m, boolean r) {
            return m<25 && !r;
        }
    }

    static class ExternalWeather {
        boolean rain() { return false; }
    }

    interface WeatherService { boolean checkRain(); }

    static class WeatherAdapter implements WeatherService {
        ExternalWeather w = new ExternalWeather();
        public boolean checkRain() { return w.rain(); }
    }

    public static void main(String[] args) {
        System.out.println(FarmDataManager.obj==FarmDataManager.obj);

        Sensor s=SensorFactory.create("soil");
        Publisher p=new Publisher();
        p.add(new Farmer());
        p.add(new Dashboard());

        Strategy st=new Normal();
        WeatherService w=new WeatherAdapter();

        double m=s.read();
        boolean on=st.check(m,w.checkRain());

        System.out.println(on?"Pump ON":"Pump OFF");
        p.notifyAll(on?"Irrigation ON":"Irrigation OFF");
    }
}
