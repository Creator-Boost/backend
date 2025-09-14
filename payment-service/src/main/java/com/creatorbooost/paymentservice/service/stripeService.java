package com.creatorbooost.paymentservice.service;

import com.creatorbooost.paymentservice.dto.ProductRequest;
import com.creatorbooost.paymentservice.dto.StripeResponse;


import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
@Service
public class stripeService {
    @Value("${stripe.secretKey:${STRIPE_SECRET_KEY:}}")
    private String secretKey;
    
    @Value("${payment.success.url}")
    private String successUrl;
    
    @Value("${payment.cancel.url}")
    private String cancelUrl;
    //stripe-API
    //->productName,amount ,quantity , currency
    //-> return sessionId and url
    public StripeResponse checkoutProducts(ProductRequest productRequest){
        // Validate URLs
        if (!StringUtils.hasText(successUrl) || !StringUtils.hasText(cancelUrl)) {
            return StripeResponse.builder()
                    .status("error")
                    .message("Payment URLs not configured properly")
                    .build();
        }
        
        // Validate Stripe secret key
        if (!StringUtils.hasText(secretKey)) {
            return StripeResponse.builder()
                    .status("error")
                    .message("Stripe secret key not configured. Please set STRIPE_SECRET_KEY environment variable.")
                    .build();
        }
        
        Stripe.apiKey=secretKey;

        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(productRequest.getName()).build();
        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(productRequest.getCurrency() == null ? "USD" : productRequest.getCurrency())
                .setUnitAmount(productRequest.getAmount())
                .setProductData(productData)
                .build();
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(productRequest.getQuantity())
                .setPriceData(priceData)
                .build();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(lineItem)
                .build();
        Session session=null;
        try {
            session=Session.create(params);
        }catch (StripeException ex){
            //log
            System.out.println("Stripe error: " + ex.getMessage());
            return StripeResponse.builder()
                    .status("error")
                    .message("Failed to create payment session: " + ex.getMessage())
                    .build();
        }
        
        if (session == null) {
            return StripeResponse.builder()
                    .status("error")
                    .message("Failed to create payment session")
                    .build();
        }
        
        return StripeResponse.builder()
                .status("success")
                .message("Payment session created successfully")
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();
    }
}
