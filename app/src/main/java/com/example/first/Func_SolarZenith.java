package com.example.first;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static java.lang.Math.PI;

class func {
    double deg2rad(double angle) {
        return (angle * PI) / 180;
    }

    double rad2deg(double rad) {
        return (180 * rad) / PI;
    }

    double get_declination(Calendar dt) {
        double day_of_year = dt.get(Calendar.YEAR);
        double fr_time_day = (dt.get(Calendar.HOUR) + dt.get(Calendar.MINUTE / 60)) / 24;
        return -23.44 * Math.cos(2 * PI * (day_of_year + fr_time_day + 10) / 365);
    }

    double get_hour_angle(Calendar dt) {
        return ((dt.HOUR + dt.MINUTE / 60) * 15) - 187.75;
    }

    double get_elevation(double decl, double lat, double hagl) {
        return Math.asin(Math.sin(decl) * Math.sin(lat) + Math.cos(decl) * Math.cos(lat) * Math.cos(hagl));
    }
}

public class Func_SolarZenith {
    func fc;
    SimpleDateFormat format;
    double lat;
    double lat_rad;
    Calendar dt;
    String format_time;
    double declination_deg;
    double declination_rad;
    double hour_angle_deg;
    double hour_angle_rad;
    double elevation_rad;
    double elevation_deg;
    double solar_zenith;

    public double getSolarZenith(double lat) {
        fc = new func();
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.lat = lat;
        lat_rad = fc.deg2rad(this.lat);

        dt = Calendar.getInstance();
        format_time = format.format(dt.getTime());

        declination_deg = fc.get_declination(dt);
        declination_rad = fc.deg2rad(declination_deg);

        hour_angle_deg = fc.get_hour_angle(dt);
        hour_angle_rad = fc.deg2rad(hour_angle_deg);

        elevation_rad = fc.get_elevation(declination_rad, lat_rad, hour_angle_rad);
        elevation_deg = fc.rad2deg(elevation_rad);

        solar_zenith = (fc.rad2deg(0.5 * PI - elevation_rad));

        return Math.round(solar_zenith * 10000) / 10000.0;  // 소수 넷째자리까지 표현
    }
}