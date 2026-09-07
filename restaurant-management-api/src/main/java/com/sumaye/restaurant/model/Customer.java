package com.sumaye.restaurant.model;
import jakarta.persistence.*; import lombok.Data; import java.time.LocalDateTime;
@Entity @Table(name="customers", uniqueConstraints=@UniqueConstraint(columnNames={"restaurant_id","phone_number"})) @Data
public class Customer { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="restaurant_id",nullable=false) private Restaurant restaurant;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="branch_id") private Branch branch;
 @Column(name="full_name",nullable=false,length=150) private String fullName; @Column(name="phone_number",nullable=false,length=20) private String phoneNumber;
 private String email; @Column(length=500) private String address; @Column(length=500) private String notes; private boolean active=true;
 private LocalDateTime createdAt=LocalDateTime.now(); private LocalDateTime updatedAt; }
