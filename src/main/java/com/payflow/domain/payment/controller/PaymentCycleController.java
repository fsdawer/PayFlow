package com.payflow.domain.payment.controller;

import com.payflow.domain.payment.service.PaymentCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/PaymentCycle")
@RequiredArgsConstructor
public class PaymentCycleController {

  private final PaymentCycleService paymentCycleService;

}
