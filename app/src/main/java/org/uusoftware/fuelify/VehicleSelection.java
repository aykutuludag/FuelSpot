package org.uusoftware.fuelify;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class VehicleSelection extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner spinner, spinner2;

    String[] acura_models = {"RSX"};
    String[] alfaRomeo_models = {"33", "75", "145", "146", "147", "155", "156", "159", "164", "166", "Brera", "Giulia", "Giulietta", "GT", "MiTo", "Spider"};
    String[] anadol_models = {"A"};
    String[] astonMartin_models = {"DB7", "DB9", "DB11", "DBS", "Rapide", "Vanquish", "Vantage", "Virage"};
    String[] audi_models = {"A1", "A3", "A4", "A5", "A6", "A7", "A8", "R8", "RS", "TT", "80 Series", "90 Series", "100 Series", "200 Series"};
    String[] bentley_models = {"Arnage", "Bentayga", "Continental", "Flying Spur", "Mulsanne"};
    String[] bmw_models = {"1 Series", "2 Series", "3 Series", "4 Series", "5 Series", "6 Series", "7 Series", "8 Series", "i Series", "M Series", "Z Series"};
    String[] bugatti_models = {"Chiron"};
    String[] buick_models = {"Century", "Park Avenue", "Regal", "Roadmaster"};
    String[] cadillac_models = {"BLS", "Brougham", "CTS", "DeVille", "Fleetwood", "Seville", "STS"};
    String[] cherry_models = {"Alia", "Chance", "Kimo", "Niche"};
    String[] chevrolet_models = {"Aveo", "Camaro", "Caprice", "Celebrity", "Corvette", "Cruze", "Epica", "Evanda", "Geo Storm", "Impala", "Kalos", "Lacetti", "Rezzo", "Spark"};
    String[] chyrsler_models = {"300 C", "300 M", "Concorde", "Crossfire", "Le Baron", "LHS", "Neon", "PT Cruiser", "Sebring", "Stratus"};
    String[] citroen_models = {"BX", "C-Elysée", "C1", "C2", "C3", "C3 Picasso", "C4", "C4 Picasso", "C4 Grand Picasso", "C5", "C6", "C8", "Evasion", "Saxo", "Xantia", "XM", "Xsara", "ZX"};
    String[] dacia_models = {"1304", "1310", "Lodgy", "Logan", "Sandero", "Solenza"};
    String[] daeweo_models = {"Chairman", "Espero", "Lanos", "Leganza", "Matiz", "Nexia", "Nubira", "Racer", "Super Saloon", "Tico"};
    String[] daihatsu_models = {"Applause", "Charade", "Cuore", "Materia", "Move", "Sirion", "YRV"};
    String[] dodge_models = {"Avenger", "Challenger", "Charger", "Intrepid", "Magnum", "Viper"};
    String[] ds_models = {"DS3", "DS4", "DS4 Crossback", "DS5"};
    String[] eagle_models = {"Talon"};
    String[] ferrari_models = {"360", "430", "456", "488", "458", "512", "575", "599", "612", "California", "F355", "FF", "F Series"};
    String[] fiat_models = {"124 Spider", "126 Bis", "500 Family", "Albea", "Barchetta", "Brava", "Bravo", "Coupe", "Croma", "Egea", "Idea", "Linea", "Marea", "Mirafiori", "Palio", "Panda", "Punto", "Regata", "Sedici", "Seicento", "Siena", "Stilo", "Tempra", "Tipo", "Ulvsse"};
    String[] ford_models = {"B-Max", "C-Max", "Cougar", "Crown Victoria", "Escort", "Festiva", "Fiesta", "Focus", "Fusion", "Galaxy", "Granada", "Granada C-Max", "Ka", "Mondeo", "Mustang", "Probe", "Puma", "Scorpio", "Sierra", "Taunus", "Taurus", "Thunderbird"};
    String[] gaz_models = {"Volga"};
    String[] geely_models = {"Echo", "Emgrand", "Familia", "FC"};
    String[] honda_models = {"Accord", "City", "Civic", "CRX", "CR-Z", "Integra", "Jazz", "Legend", "Prelude", "S2000", "Shuttle", "S-MX", "Stream"};
    String[] hyundai_models = {"Accent", "Accent Blue", "Accent Era", "Atos", "Coupe", "Dynasty", "Elentra", "Excel", "Genesis", "Getz", "Grandeur", "Ioniq", "i10", "i20", "i20 Active", "i20 Troy", "i30", "i40", "iX200", "Matrix", "S-Coupe", "Sonata", "Trajet"};
    String[] ikco_models = {"Samand"};
    String[] infiniti_models = {"G", "I30", "Q30", "Q50"};
    String[] isuzu_models = {"Gemini"};
    String[] jaguar_models = {"Daimler", "F-Type", "Sovereign", "S-Type", "XE", "XF", "XJ", "XJR", "XJS", "XK8", "XKR", "X-Type"};
    String[] kia_models = {"Capital", "Carens", "Ceed", "Cerato", "Clarus", "Magentis", "Opirus", "Optima", "Picanto", "Pride", "Pro Ceed", "Rio", "Sephia", "Shuma", "Soul", "Venga"};
    String[] kral_models = {"Grande-5"};
    String[] lada_models = {"Kalina", "Nova", "Priora", "Samara", "Tavria", "Vaz", "Vega"};
    String[] lamborghini_models = {"Aventador", "Gallardo", "Huracan"};
    String[] lancia_models = {"Delta", "Thema", "Y (Ypsilon)"};
    String[] lexus_models = {"IS", "LS", "RC"};
    String[] lincoln_models = {"Continental", "LS", "MKS", "Town Car"};
    String[] lotus_models = {"Elisa", "Esprit"};
    String[] maserati_models = {"Series 4", "Cambiocorsa", "Ghibli", "GranCabrio", "GranTurismo", "GT", "Quattroporte", "Spyder"};
    String[] maybach_models = {"62"};
    String[] mazda_models = {"2", "3", "5", "6", "121", "323", "626", "929", "Lantis", "MX", "Premacy", "RX", "Xedos"};
    String[] mercedes_models = {"A", "AMG GT", "B", "C", "CL", "CLA", "CLC", "CLK", "CLS", "E", "Maybach S", "R", "S", "SL", "SLC", "SLK", "SLS AMG", "190", "200", "220", "230", "240", "250", "260", "280", "300", "400", "420", "500", "560"};
    String[] mercury_models = {"Sable"};
    String[] mg_models = {"F", "ZR"};
    String[] mini_models = {"Cooper", "Cooper Clubman", "Cooper S", "John Cooper", "One"};
    String[] mitsubishi_models = {"Attrage", "Carisma", "Colt", "Eclipse", "Galant", "Grandis", "Lancer", "Lancer Evolution", "Space Star", "Space Wagon"};
    String[] moskwitsch_models = {"1500 SL", "Aleko"};
    String[] nissan_models = {"200 SX", "350 Z", "Almera", "Bluebird", "GT-R", "Maxima", "Micra", "Note", "NX Coupe", "Primera", "Pulsar", "Sunny"};
    String[] oldsmobile_models = {"Cutlass Ciera"};
    String[] opel_models = {"Adam", "Agila", "Ampera", "Ascona", "Astra", "Calibra", "Cascada", "Corsa", "GT (Roadster)", "Insignia", "Insignia Grand Sport", "Insignia Sports Tourer", "Kadett", "Manta", "Meriva", "Omega", "Rekord", "Senator", "Signum", "Tigra", "Vectra", "Zafira"};
    String[] pagani_models = {"Huayra"};
    String[] peugeot_models = {"106", "107", "205", "206", "206+", "207", "208", "301", "305", "306", "307", "308", "309", "405", "406", "407", "508", "605", "607", "806", "807", "RCZ"};
    String[] plymouth_models = {"Laser"};
    String[] pontiac_models = {"Firebird", "Grand Am", "Sunbird"};
    String[] porsche_models = {"718", "911", "928", "944", "Boxster", "Cayman", "Panamera"};
    String[] proton_models = {"218", "315", "415", "416", "418", "420", "Gen-2", "Persona", "Saga", "Savvy", "Waja"};
    String[] renault_models = {"Clio", "Espace", "Fluence", "Fluence Z.E.", "Laguna", "Latitude", "Megane", "Modus", "Safrane", "Symbol", "Twizy", "ZOE", "Scenic", "Grand Scenic", "Talisman", "Twingo", "R 5", "R 9", "R 11", "R 12", "R 19", "R 21", "R 25"};
    String[] rollsRoyce_models = {"Ghost", "Park Ward", "Phantom", "Wraith"};
    String[] rover_models = {"25", "45", "75", "200", "214", "216", "218", "220", "400", "414", "416", "420", "620", "820", "Streetwise"};
    String[] saab_models = {"900", "9000", "9-3", "9-5"};
    String[] seat_models = {"Alhambra", "Altea", "Exeo", "Ibiza", "Leon", "Marbella", "Toledo"};
    String[] skoda_models = {"Citigo", "Fabia", "Favorit", "Felicia", "Forman", "Octavia", "Rapid", "Roomster", "Superb"};
    String[] smart_models = {"Forfour", "Fortwo", "Roadster"};
    String[] subaru_models = {"BRZ", "Impreza", "Justy", "Legacy", "Levorg", "SVX", "Vivio"};
    String[] suzuki_models = {"Alto", "Baleno", "Ignis", "Liana", "Maruti", "S-Cross", "Splash", "Swift", "SX4"};
    String[] tata_models = {"Indica", "Indigo", "Manza", "Marina", "Vista"};
    String[] tesla_models = {"Model S", "Model X"};
    String[] tofas_models = {"Doğan", "Kartal", "Murat", "Serçe", "Şahin"};
    String[] toyota_models = {"Auris", "Avensis", "Camry", "Carina", "Celica", "Corolla", "Corona", "Cressida", "Grown", "GT 86", "MR2", "Prius", "Starlet", "Supra", "Tercel", "Urban Cruiser", "Verso", "Yaris"};
    String[] vw_models = {"Arteon", "Bora", "EOS", "Golf", "Jetta", "Lupo", "New Beetle", "The Beetle", "Passat", "Passat Variant", "Phaeton", "Polo", "Santana", "Scirocco", "Sharan", "Touran", "Vento", "VW CC"};
    String[] volvo_models = {"C30", "C70", "S40", "S60", "S70", "S80", "S90", "V40", "V40 Cross Country", "V50", "V60", "V70", "V90 Cross Country", "240", "244", "440", "460", "480", "740", "850", "940", "960"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_selection);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //MARKA SEÇİMİ
        spinner = findViewById(R.id.spinner_brands);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.car_makers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //MODEL
        spinner2 = findViewById(R.id.spinner_models);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.spinner_brands) {
            Toast.makeText(VehicleSelection.this, "MARKA: " + spinner.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
            ArrayAdapter<String> adapter2 = null;
            switch (position) {
                case 0:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, acura_models);
                    break;
                case 1:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, alfaRomeo_models);
                    break;
                case 2:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, anadol_models);
                    break;
                case 3:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, astonMartin_models);
                    break;
                case 4:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, audi_models);
                    break;
                case 5:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bentley_models);
                    break;
                case 6:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bmw_models);
                    break;
                case 7:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bugatti_models);
                    break;
                case 8:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, buick_models);
                    break;
                case 9:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cadillac_models);
                    break;
                case 10:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cherry_models);
                    break;
                case 11:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, chevrolet_models);
                    break;
                case 12:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, chyrsler_models);
                    break;
                case 13:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, citroen_models);
                    break;
                case 14:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dacia_models);
                    break;
                case 15:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daeweo_models);
                    break;
                case 16:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daihatsu_models);
                    break;
                case 17:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dodge_models);
                    break;
                case 18:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ds_models);
                    break;
                case 19:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eagle_models);
                    break;
                case 20:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ferrari_models);
                    break;
                case 21:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fiat_models);
                    break;
                case 22:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ford_models);
                    break;
                case 23:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, gaz_models);
                    break;
                case 24:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, geely_models);
                    break;
                case 25:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, honda_models);
                    break;
                case 26:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hyundai_models);
                    break;
                case 27:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ikco_models);
                    break;
                case 28:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, infiniti_models);
                    break;
                case 29:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, isuzu_models);
                    break;
                case 30:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, jaguar_models);
                    break;
                case 31:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kia_models);
                    break;
                case 32:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, kral_models);
                    break;
                case 33:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lada_models);
                    break;
                case 34:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lamborghini_models);
                    break;
                case 35:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lancia_models);
                    break;
                case 36:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lexus_models);
                    break;
                case 37:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lincoln_models);
                    break;
                case 38:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lotus_models);
                    break;
                case 39:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, maserati_models);
                    break;
                case 40:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, maybach_models);
                    break;
                case 41:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mazda_models);
                    break;
                case 42:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mercedes_models);
                    break;
                case 43:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mercury_models);
                    break;
                case 44:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mg_models);
                    break;
                case 45:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mini_models);
                    break;
                case 46:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mitsubishi_models);
                    break;
                case 47:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, moskwitsch_models);
                    break;
                case 48:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nissan_models);
                    break;
                case 49:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, oldsmobile_models);
                    break;
                case 50:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opel_models);
                    break;
                case 51:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pagani_models);
                    break;
                case 52:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, peugeot_models);
                    break;
                case 53:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, plymouth_models);
                    break;
                case 54:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, pontiac_models);
                    break;
                case 55:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, porsche_models);
                    break;
                case 56:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, proton_models);
                    break;
                case 57:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, renault_models);
                    break;
                case 58:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rollsRoyce_models);
                    break;
                case 59:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rover_models);
                    break;
                case 60:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, saab_models);
                    break;
                case 61:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, seat_models);
                    break;
                case 62:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, skoda_models);
                    break;
                case 63:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, smart_models);
                    break;
                case 64:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subaru_models);
                    break;
                case 65:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, suzuki_models);
                    break;
                case 66:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tata_models);
                    break;
                case 67:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tesla_models);
                    break;
                case 68:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tofas_models);
                    break;
                case 69:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, toyota_models);
                    break;
                case 70:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vw_models);
                    break;
                case 71:
                    adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, volvo_models);
                    break;
                default:
                    break;
            }

            if (adapter2 != null) {
                adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner2.setAdapter(adapter2);
                spinner2.setOnItemSelectedListener(this);
            }
        } else if (spinner.getId() == R.id.spinner_models) {
            Toast.makeText(VehicleSelection.this, "MODEL: " + spinner.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}