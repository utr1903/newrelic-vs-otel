package com.newrelic.futurestack.istanbul.proxy.service.list.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PipelineData {

  private long id;
  private String name;
  private String value;
}
