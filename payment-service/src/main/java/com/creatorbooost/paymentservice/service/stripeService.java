package com.creatorbooost.paymentservice.service;

import com.creatorbooost.paymentservice.dto.ProductRequest;
import com.creatorbooost.paymentservice.dto.StripeResponse;


import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class stripeService {
    @Value("${stripe.secretKey}")
    private String secretKey;
    //stripe-API
    //->productName,amount ,quantity , currency
    //-> return sessionId and url
    public StripeResponse checkoutProducts(ProductRequest productRequest){
        Stripe.apiKey=secretKey;

        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(productRequest.getName()).build();
        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(productRequest.getCurrency() == null ? "USD" : productRequest.getCurrency())
                .setUnitAmount(productRequest.getAmount()*100)
                .setProductData(productData)
                .build();
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(productRequest.getQuantity())
                .setPriceData(priceData)
                .build();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5173/payment/success")
                .setCancelUrl("http://localhost:5173/payment/failure")
                .addLineItem(lineItem)
                .build();
        Session session=null;
        try {
            session=Session.create(params);
        }catch (StripeException ex){
            //log
            System.out.println(ex.getMessage());
        }
        return StripeResponse.builder()
                .status("success")
                .message(" payment success created")
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .build();
    }
}
