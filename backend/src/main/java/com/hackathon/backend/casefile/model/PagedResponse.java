package com.hackathon.backend.casefile.model;

import java.util.List;

public record PagedResponse<T>(
  List<T> data,
  int total,
  boolean success
) {
}
