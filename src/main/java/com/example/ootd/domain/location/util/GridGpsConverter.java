package com.example.ootd.domain.location.util;

public class GridGpsConverter {

  public static final int MODE_GRID = 1;
  public static final int MODE_GPS = 0;

  public static LatXLngY convertGRID_GPS(int mode, double lat, double lng) {
    final double RE = 6371.00877;  // 지구 반경(km)
    final double GRID = 5.0;       // 격자 간격(km)
    final double SLAT1 = 30.0;     // 투영 위도1(degree)
    final double SLAT2 = 60.0;     // 투영 위도2(degree)
    final double OLON = 126.0;     // 기준점 경도(degree)
    final double OLAT = 38.0;      // 기준점 위도(degree)
    final double XO = 43.0;        // 기준점 X좌표(GRID)
    final double YO = 136.0;       // 기준점 Y좌표(GRID)

    final double DEGRAD = Math.PI / 180.0;
    final double RADDEG = 180.0 / Math.PI;

    double re = RE / GRID;
    double slat1 = SLAT1 * DEGRAD;
    double slat2 = SLAT2 * DEGRAD;
    double olon = OLON * DEGRAD;
    double olat = OLAT * DEGRAD;

    double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);

    double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
    sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

    double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
    ro = re * sf / Math.pow(ro, sn);

    LatXLngY rs = new LatXLngY();

    if (mode == MODE_GRID) {
      rs.lat = lat;
      rs.lng = lng;

      double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
      ra = re * sf / Math.pow(ra, sn);
      double theta = lng * DEGRAD - olon;

      if (theta > Math.PI) {
        theta -= 2.0 * Math.PI;
      }
      if (theta < -Math.PI) {
        theta += 2.0 * Math.PI;
      }
      theta *= sn;

      rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
      rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

    } else {
      rs.x = lat;
      rs.y = lng;

      double xn = lat - XO;
      double yn = ro - lng + YO;

      double ra = Math.sqrt(xn * xn + yn * yn);
      if (sn < 0.0) {
        ra = -ra;
      }

      double alat = Math.pow((re * sf / ra), (1.0 / sn));
      alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

      double theta;
      if (Math.abs(xn) <= 0.0) {
        theta = 0.0;
      } else {
        if (Math.abs(yn) <= 0.0) {
          theta = Math.PI * 0.5;
          if (xn < 0.0) {
            theta = -theta;
          }
        } else {
          theta = Math.atan2(xn, yn);
        }
      }

      double alon = theta / sn + olon;
      rs.lat = alat * RADDEG;
      rs.lng = alon * RADDEG;
    }

    return rs;
  }

  public static class LatXLngY {

    public Double lat;
    public Double lng;
    public Double x;
    public Double y;

    public LatXLngY() {
    }

    public LatXLngY(Double lat, Double lng, Double x, Double y) {
      this.lat = lat;
      this.lng = lng;
      this.x = x;
      this.y = y;
    }
  }
}
