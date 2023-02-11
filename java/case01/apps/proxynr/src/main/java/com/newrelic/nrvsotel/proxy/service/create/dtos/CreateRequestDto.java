package com.newrelic.nrvsotel.proxy.service.create.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequestDto {

  private String name;

  private String value;
}
