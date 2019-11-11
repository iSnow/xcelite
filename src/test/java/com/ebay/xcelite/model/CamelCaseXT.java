package com.ebay.xcelite.model;

import com.ebay.xcelite.annotations.Column;

import java.util.Date;

public class CamelCaseXT extends CamelCase{

    @Column
    private String testColNoAnnotationName1;

    @Column(name = "Name")
    private String name;

    @Column(name = "CompletelyDifferentColName")
    private String surname;

    @Column(dataFormat = UsStringCellDateConverter.DATE_PATTERN, name = "BirthDate", converter = UsStringCellDateConverter.class)
    private Date birthDate;

    @Column(name = "Sex")
    private String sex;

    private String testColNoAnnotation2;

}
