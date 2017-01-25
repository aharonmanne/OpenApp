package com.ksdagile.openapp;


import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */

public class MapViewFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    MapView mapView;
    private GoogleMap googleMap;
    GateSettings settings;
    ConfigActivity parentActivity;

    public MapViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map_view, container, false);
        settings = GateSettings.GetInstance(getActivity());

        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        parentActivity = (ConfigActivity) getActivity();
        settings.SetLatitude(marker.getPosition().latitude);
        settings.SetLongitude(marker.getPosition().longitude);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap = googleMap;
        int zoomVal = 15;
        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException ex) {
            Log.d("MapViewFragment", ex.getMessage());
        }

        // For dropping a marker at a point on the Map
        double latitude = settings.GetLatitude();
        double longitude = settings.GetLongitude();
        LatLng gate = null;
        if (latitude > GateSettings.MAX_LAT ||
                longitude > GateSettings.MAX_LONG) {
            zoomVal = 5;
            gate = GetDefaultLoc();
            settings.SetLatitude(gate.latitude);
            settings.SetLongitude(gate.longitude);
        } else {
            gate = new LatLng(latitude, longitude);
        }

        googleMap.addMarker(new MarkerOptions().position(gate).title("Gate").snippet("Set Gate Location").draggable(true));
        googleMap.setOnMarkerDragListener(this);

        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(gate).zoom(zoomVal).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private LatLng GetDefaultLoc() {
        double latitude = 46.2083546;
        double longitude = 6.1227166; // Switzerland is neutral
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = getResources().getConfiguration().locale;
        }
        String localeString = locale.getISO3Country();
        switch (localeString) {
            case "USA": {
                latitude = 40.705565;
                longitude = -74.1180854;
                break;
            }
            case "IND": {
                latitude = 28.527272;
                longitude = 77.1389454;
                break;
            }
            case "ISR": {
                latitude = 31.7962994;
                longitude = 35.1053194;
                break;
            }
            case "FRA": {
                latitude = 48.8588377;
                longitude = 2.2775177;
                break;
            }
            case "GBR": {
                latitude = 51.5285582;
                longitude = -0.2416794;
                break;
            }
            case "ALA":
            case "ALB":
            case "DZA":
            case "AND":
            case "AIA":
            case "ATA":
            case "ATG":
            case "ARG":
            case "ARM":
            case "AUS":
            case "AUT":
            case "AZE":
            case "BHS":
            case "BHR":
            case "BGD":
            case "BRB":
            case "BLR":
            case "BEL":
            case "BLZ":
            case "BEN":
            case "BMU":
            case "BTN":
            case "BOL":
            case "BIH":
            case "BWA":
            case "BVT":
            case "BRA":
            case "VGB":
            case "IOT":
            case "BRN":
            case "BGR":
            case "BFA":
            case "BDI":
            case "KHM":
            case "CMR":
            case "CAN":
            case "CPV":
            case "CYM":
            case "CAF":
            case "TCD":
            case "CHL":
            case "CHN":
            case "HKG":
            case "MAC":
            case "CXR":
            case "CCK":
            case "COL":
            case "COM":
            case "COG":
            case "COD":
            case "COK":
            case "CRI":
            case "CIV":
            case "HRV":
            case "CUB":
            case "CYP":
            case "CZE":
            case "DNK":
            case "DJI":
            case "DMA":
            case "DOM":
            case "ECU":
            case "EGY":
            case "SLV":
            case "GNQ":
            case "ERI":
            case "EST":
            case "ETH":
            case "FLK":
            case "FRO":
            case "FJI":
            case "FIN":
            case "GUF":
            case "PYF":
            case "ATF":
            case "GAB":
            case "GMB":
            case "GEO":
            case "DEU":
            case "GHA":
            case "GIB":
            case "GRC":
            case "GRL":
            case "GRD":
            case "GLP":
            case "GUM":
            case "GTM":
            case "GGY":
            case "GIN":
            case "GNB":
            case "GUY":
            case "HTI":
            case "HMD":
            case "VAT":
            case "HND":
            case "HUN":
            case "IDN":
            case "IRN":
            case "IRQ":
            case "IRL":
            case "IMN":
            case "ISL":
            case "ITA":
            case "JAM":
            case "JPN":
            case "JEY":
            case "JOR":
            case "KAZ":
            case "KEN":
            case "KIR":
            case "PRK":
            case "KOR":
            case "KWT":
            case "KGZ":
            case "LAO":
            case "LVA":
            case "LBN":
            case "LSO":
            case "LBR":
            case "LBY":
            case "LIE":
            case "LTU":
            case "LUX":
            case "MKD":
            case "MDG":
            case "MWI":
            case "MYS":
            case "MDV":
            case "MLI":
            case "MLT":
            case "MHL":
            case "MTQ":
            case "MRT":
            case "MUS":
            case "MYT":
            case "MEX":
            case "FSM":
            case "MDA":
            case "MCO":
            case "MNG":
            case "MNE":
            case "MSR":
            case "MAR":
            case "MOZ":
            case "MMR":
            case "NAM":
            case "NRU":
            case "NPL":
            case "NLD":
            case "ANT":
            case "NCL":
            case "NZL":
            case "NIC":
            case "NER":
            case "NGA":
            case "NIU":
            case "NFK":
            case "MNP":
            case "NOR":
            case "OMN":
            case "PAK":
            case "PLW":
            case "PSE":
            case "PAN":
            case "PNG":
            case "PRY":
            case "PER":
            case "PHL":
            case "PCN":
            case "POL":
            case "PRT":
            case "PRI":
            case "QAT":
            case "REU":
            case "ROU":
            case "RUS":
            case "RWA":
            case "BLM":
            case "SHN":
            case "KNA":
            case "LCA":
            case "MAF":
            case "SPM":
            case "VCT":
            case "WSM":
            case "SMR":
            case "STP":
            case "SAU":
            case "SEN":
            case "SRB":
            case "SYC":
            case "SLE":
            case "SGP":
            case "SVK":
            case "SVN":
            case "SLB":
            case "SOM":
            case "ZAF":
            case "SGS":
            case "SSD":
            case "ESP":
            case "LKA":
            case "SDN":
            case "SUR":
            case "SJM":
            case "SWZ":
            case "SWE":
            case "CHE":
            case "SYR":
            case "TWN":
            case "TJK":
            case "TZA":
            case "THA":
            case "TLS":
            case "TGO":
            case "TKL":
            case "TON":
            case "TTO":
            case "TUN":
            case "TUR":
            case "TKM":
            case "TCA":
            case "TUV":
            case "UGA":
            case "UKR":
            case "ARE":
            case "UMI":
            case "URY":
            case "UZB":
            case "VUT":
            case "VEN":
            case "VNM":
            case "VIR":
            case "WLF":
            case "ESH":
            case "YEM":
            case "ZMB":
            case "ZWE":
            default:
                // don't change
        }
        return new LatLng(latitude, longitude);
    }
}
