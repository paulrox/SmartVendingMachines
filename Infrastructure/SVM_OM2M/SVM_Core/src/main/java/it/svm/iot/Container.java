package it.svm.iot;

/**
 * Class representing a Container on the CSE
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */

import java.util.List;

public class Container {
	private String rn;
	private int ty;
	private String ri;
	public String getRn() {
		return rn;
	}
	public void setRn(String rn) {
		this.rn = rn;
	}
	public int getTy() {
		return ty;
	}
	public void setTy(int ty) {
		this.ty = ty;
	}
	public String getRi() {
		return ri;
	}
	public void setRi(String ri) {
		this.ri = ri;
	}
	public String getPi() {
		return pi;
	}
	public void setPi(String pi) {
		this.pi = pi;
	}
	public String getCt() {
		return ct;
	}
	public void setCt(String ct) {
		this.ct = ct;
	}
	public String getLt() {
		return lt;
	}
	public void setLt(String lt) {
		this.lt = lt;
	}
	public List<String> getAcpi() {
		return acpi;
	}
	public void setAcpi(List<String> acpi) {
		this.acpi = acpi;
	}
	public String getEt() {
		return et;
	}
	public void setEt(String et) {
		this.et = et;
	}
	public int getSt() {
		return st;
	}
	public void setSt(int st) {
		this.st = st;
	}
	public int getMni() {
		return mni;
	}
	public void setMni(int mni) {
		this.mni = mni;
	}
	public int getMia() {
		return mia;
	}
	public void setMia(int mia) {
		this.mia = mia;
	}
	public int getCni() {
		return cni;
	}
	public void setCni(int cni) {
		this.cni = cni;
	}
	public int getCbs() {
		return cbs;
	}
	public void setCbs(int cbs) {
		this.cbs = cbs;
	}
	public String getOl() {
		return ol;
	}
	public void setOl(String ol) {
		this.ol = ol;
	}
	public String getLa() {
		return la;
	}
	public void setLa(String la) {
		this.la = la;
	}
	private String pi;
	private String ct;
	private String lt;
	private List<String> acpi;
	private String et;
	private int st;
	private int mni;
	private int mia;
	private int cni;
	private int cbs;
	private String ol;
	private String la;
}
