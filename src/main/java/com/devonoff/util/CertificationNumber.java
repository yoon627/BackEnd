package com.devonoff.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CertificationNumber {

  public static String getCertificationNumber() {

    StringBuilder certificationNumber = new StringBuilder();

    for (int count = 0; count < 4; count++) {
      certificationNumber.append((int) (Math.random() * 10));
    }

    return certificationNumber.toString();

  }

}
