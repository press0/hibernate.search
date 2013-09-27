package net.timontech.hibernate.search;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

@Entity
@Indexed
@Analyzer(impl = org.apache.lucene.analysis.standard.StandardAnalyzer.class)
public class Car {

	@Id
	@GeneratedValue
	@DocumentId
	private Long id;

	@Column
	@Field(store = Store.YES)
	private String make;

	@Column
	@Field(store = Store.YES)
	private String model;

	@Column
	@Field(store = Store.YES)
	private short year;

	@Column
	@Field(store = Store.NO)
	private String description;

	public Car() {
	}

	public Car(String make, String model, short year, String description) {
		super();
		this.make = make;
		this.model = model;
		this.year = year;
		this.description = description;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public short getYear() {
		return year;
	}

	public void setYear(short year) {
		this.year = year;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
