package com.ugopiemontese.opendatalecce.utils;

public class Incidenti {
		 
    private int id;
    private String address;
    private Double lat, lng;
    private int totale, coinvolti, illesi, morti, prognosi, feriti;
 
    public Incidenti(){}
 
    public Incidenti(String address, Double lat, Double lng, int totale, int coinvolti, int illesi, int morti, int prognosi, int feriti) {
    	
        super();
        
        this.address = address;
        this.totale = totale;
        this.lat = lat;
        this.lng = lng;
        this.coinvolti = coinvolti;
        this.illesi = illesi;
        this.morti = morti;
        this.prognosi = prognosi;
        this.feriti = feriti;
        
    }
  
    @Override
    public String toString() {
        return "Via [" +
        		"id=" + id + 
        		", address=" + address + 
        		", lat=" + lat + 
        		", lng=" + lng + 
        		", totale=" + totale + 
        		", coinvolti=" + coinvolti + 
        		", illesi=" + illesi + 
        		", morti=" + morti +
        		", prognosi=" + prognosi +
        		", feriti=" + feriti +
        		"]";
    }
    
    public String getAddress() {
    	return this.address;
    }
    
    public Double getLat() {
    	return this.lat;
    }
    
    public Double getLng() {
    	return this.lng;
    }
    
    public int getTotale() {
    	return this.totale;
    }
    
    public int getCoinvolti() {
    	return this.coinvolti;
    }
    
    public int getIllesi() {
    	return this.illesi;
    }
    
    public int getMorti() {
    	return this.morti;
    }
    
    public int getPrognosi() {
    	return this.prognosi;
    }
    
    public int getFeriti() {
    	return this.feriti;
    }
	
    public void setId(int id) {
    	this.id = id;
    }
    
    public void setAddress(String address) {
    	this.address = address;
    }
    
    public void setLat(Double lat) {
    	this.lat = lat;
    }
    
    public void setLng(Double lng) {
    	this.lng = lng;
    }
    
    public void setTotale(int totale) {
    	this.totale = totale;
    }
    
    public void setCoinvolti(int coinvolti) {
    	this.coinvolti = coinvolti;
    }
    
    public void setIllesi(int illesi) {
    	this.illesi = illesi;
    }
    
    public void setMorti(int morti) {
    	this.morti = morti;
    }
    
    public void setPrognosi(int prognosi) {
    	this.prognosi = prognosi;
    }
    
    public void setFeriti(int feriti) {
    	this.feriti = feriti;
    }
    
}
