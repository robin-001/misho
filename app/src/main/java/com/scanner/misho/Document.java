package com.scanner.misho;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.scanner.misho.mrz.MrzParser;
import com.scanner.misho.mrz.MrzRecord;

import org.jmrtd.lds.icao.MRZInfo;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;


public class Document {
    private static final String TAG = "DOCUMENT";
    public static int PASSPORT=1;
    public static int NATIONAL_ID=2;
    public static int DRIVER_LICENSE=3;
    public static int PHOTO_ID=4;
    public static int VISA=5;

    private int id;
    private int type;
    private String document_number;
    private String identification_number;
    private String firstname;
    private String surname;
    private String givennames;
    private String gender;
    private Date dob;
    private Date issuing_date;
    private Date expiry_date;
    private String issuing_authority;
    private String nationality;
    private String latitude;
    private String longitude;
    private String imei;
    private String created_by;
    private String front_photo;
    private String back_photo;
    private String extra0;
    private String extra1;
    private String extra2;
    private  String raw_data;
    private boolean synced =false;
    private Date created_at;
    private String trx_id;


    public Document() {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String base64_decode(String encodedString){
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        String decodedString = new String(decodedBytes);
        return decodedString;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Document(int type, String raw_data) throws Exception {
        this.trx_id = UUID.randomUUID().toString().replace("-", "");
        this.created_at=new Date();
        this.type = type;
        this.synced=false;
        this.created_by = "1";
        this.raw_data = raw_data;
        this.decode_document();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void decode_document() throws Exception {
        switch (this.type){
            case 1:
                createPassport();
                break;

            case 2://Ugandan national ID. Function to be refined to capture other national IDs
                createNationalID();
                break;

            case 3:
                break;

            case 4:
                break;

            default:
                break;
        }


    }

    private void createPassport() throws ParseException {
        MRZInfo mrzInfo = new MRZInfo(this.raw_data);
        MrzRecord record = MrzParser.parse(mrzInfo.toString());
        this.document_number=record.documentNumber;
        this.surname=record.surname;
        this.givennames=record.givenNames;
        this.gender=record.sex.toString();

        SimpleDateFormat sdf = new SimpleDateFormat("{d/M/y}");
        Log.d("MRZ date",record.dateOfBirth.toString());
        Log.d("MRZ date",Integer.toString(record.dateOfBirth.day));
        Log.d("MRZ date",Integer.toString(record.dateOfBirth.month));
        Log.d("MRZ date",Integer.toString(record.dateOfBirth.year));

        this.dob=sdf.parse(record.dateOfBirth.toString());
        this.expiry_date=sdf.parse(record.expirationDate.toString());
        this.nationality=record.nationality;
        this.issuing_authority=record.issuingCountry;
        this.extra0=record.code.toString();
        this.extra2=Character.toString(record.code2);
        this.extra1=Character.toString(record.code1);

        Log.d(TAG,record.format.toString());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNationalID() throws Exception {
        String[] idContents = this.raw_data.split(";");
        if(idContents.length!=11){
            throw new Exception("Invalid National ID format");
        }

        this.issuing_authority="UGA";
        this.nationality="UGA";
        this.surname = base64_decode(idContents[0]);
        this.givennames = base64_decode(idContents[1]);
        this.gender = idContents[6].startsWith("CF")?"F":"M";
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");

        this.dob  = sdf.parse(idContents[3]);
        this.issuing_date = sdf.parse(idContents[4]);
        this.expiry_date = sdf.parse(idContents[5]);

        this.identification_number = idContents[6];
        this.document_number = idContents[7];
        this.extra0=idContents[8];
        this.extra1=idContents[9];
        this.extra2=idContents[10];
    }

    public Document(int type, String document_number, String surname, String givennames) {
        this.type = type;
        this.surname = surname;
        this.givennames = givennames;
        this.document_number = document_number;
        this.gender = document_number.startsWith("CM") ? "M" : "F";
    }


    public Document(int type, String firstname, String surname, String givennames, Date dob, String issuer, String nationality, String gender, Date issuing_date, Date expiry_date, String document_number) {
        this.type = type;
        this.firstname = firstname;
        this.surname = surname;
        this.givennames = givennames;
        this.dob = dob;
        this.issuing_authority = issuer;
        this.nationality = nationality;
        this.gender = gender;
        this.issuing_date = issuing_date;
        this.expiry_date = expiry_date;
        this.document_number = document_number;

    }

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", type=" + trx_id +
                ", type=" + type +
                ", document_number='" + document_number + '\'' +
                ", identification_number='" + identification_number + '\'' +
                ", firstname='" + firstname + '\'' +
                ", surname='" + surname + '\'' +
                ", givennames='" + givennames + '\'' +
                ", gender='" + gender + '\'' +
                ", dob=" + dob +
                ", issuing_date=" + issuing_date +
                ", expiry_date=" + expiry_date +
                ", issuing_authority='" + issuing_authority + '\'' +
                ", nationality='" + nationality + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", imei='" + imei + '\'' +
                ", created_by='" + created_by + '\'' +
                ", front_photo='" + front_photo + '\'' +
                ", back_photo='" + back_photo + '\'' +
                ", extra0='" + extra0 + '\'' +
                ", extra1='" + extra1 + '\'' +
                ", extra2='" + extra2 + '\'' +
                ", raw_data='" + raw_data + '\'' +
                ", synced=" + synced +
                ", created_at=" + created_at +
                '}';
    }

    public String toJSON(){
        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("id", String.valueOf(getId()));
            jsonObject.put("trx_id", getTrxID());
            jsonObject.put("type", getType());
            jsonObject.put("document_number", getDocument_number());
            jsonObject.put("identification_number", getIdentification_number());
            jsonObject.put("surname", getSurname());
            jsonObject.put("firstname", getFirstname());
            jsonObject.put("given_names", getGivennames());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            jsonObject.put("dob", sdf.format(getDob()));
           if(getIssuing_date()!=null){
               jsonObject.put("issuing_date", sdf.format(getIssuing_date()));
           }
            jsonObject.put("expiry_date", sdf.format(getExpiry_date()));
            jsonObject.put("issuing_authority", getIssuing_authority());
            jsonObject.put("nationality", getNationality());
            jsonObject.put("latitude", getLatitude());
            jsonObject.put("longitude", getLongitude());
            jsonObject.put("imei", getImei());
            jsonObject.put("created_by", getCreated_by());
            jsonObject.put("front_photo", getFront_photo());
            jsonObject.put("back_photo", getBack_photo());
            jsonObject.put("extra0", getExtra0());
            jsonObject.put("extra1", getExtra1());
            jsonObject.put("extra2", getExtra2());
            jsonObject.put("raw_data", getExtra2());
            jsonObject.put("synced", isSynced());
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            jsonObject.put("created_at", sdf.format(getCreated_at()));

            return jsonObject.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public String getReadableType() {
        String readableType="";
        switch (type){
            case 1:
                readableType= "Passport";
                break;
            case 2:
                readableType= "National ID";
                break;

            case 3:
                readableType= "Driver's License";
                break;

            default:
                readableType= "Unknown";

        }
        return readableType;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGivennames() {
        return givennames;
    }

    public void setGivennames(String givennames) {
        this.givennames = givennames;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getIssuing_authority() {
        return issuing_authority;
    }

    public void setIssuer(String issuer) {
        this.issuing_authority = issuer;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getIssuing_date() {
        return issuing_date;
    }

    public void setIssuing_date(Date issuing_date) {
        this.issuing_date = issuing_date;
    }

    public Date getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(Date expiry_date) {
        this.expiry_date = expiry_date;
    }

    public String getDocument_number() {
        return document_number;
    }

    public void setDocument_number(String document_number) {
        this.document_number = document_number;
    }

    public String getIdentification_number() {
        return identification_number;
    }

    public void setIdentification_number(String document_other_number) {
        this.identification_number = document_other_number;
    }

    public String getIdentification_number_name() {
        return identification_number;
    }

    public void setIdentification_number_name(String identification_number_name) {
        this.identification_number = identification_number_name;
    }

    public String getFront_photo() {
        return front_photo;
    }

    public void setFront_photo(String front_photo) {
        this.front_photo = front_photo;
    }

    public String getBack_photo() {
        return back_photo;
    }

    public void setBack_photo(String back_photo) {
        this.back_photo = back_photo;
    }

    public void add(Document document) {
    }

    public void setIssuing_authority(String issuing_authority) {
        this.issuing_authority = issuing_authority;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public String getExtra0() {
        return extra0;
    }

    public void setExtra0(String extra0) {
        this.extra0 = extra0;
    }

    public String getExtra1() {
        return extra1;
    }

    public void setExtra1(String extra1) {
        this.extra1 = extra1;
    }

    public String getExtra2() {
        return extra2;
    }

    public void setExtra2(String extra2) {
        this.extra2 = extra2;
    }

    public String getRaw_data() {
        return raw_data;
    }

    public void setRaw_data(String raw_data) {
        this.raw_data = raw_data;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public void setTrxID(String trx_id) {
        this.trx_id = trx_id;
    }

    public String getTrxID() {
        return this.trx_id;
    }
}
