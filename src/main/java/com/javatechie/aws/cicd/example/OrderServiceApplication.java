package com.javatechie.aws.cicd.example;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
@RequestMapping("/")
public class OrderServiceApplication {

    @Autowired
    private OrderDao orderDao;

    @GetMapping("/orders")
    public List<Order> fetchOrders() {
        return orderDao.getOrders().stream().
                sorted(Comparator.comparing(Order::getPrice)).collect(Collectors.toList());
    }

    @GetMapping("/health")
    public @ResponseBody
    ResponseEntity<?> getHealth(){
        return  new ResponseEntity<>("REST end point:" + HttpStatus.OK, HttpStatus.OK);
    }

    @PostMapping("/mask-to-cidr")
    public CidirAndMaskResponse convertMaskToCidr(@RequestParam String value) throws UnknownHostException {
        InetAddress netmask = InetAddress.getByName(value);
        return new CidirAndMaskResponse("mask-to-cidr", value, convertNetmaskToCIDR(netmask));
    }

    @PostMapping("/cidr-to-mask")
    public CidirAndMaskResponse convertCidrToMas(@RequestParam String value) throws AddressStringException {
        return new CidirAndMaskResponse("cidr-to-mask", value, convertCidrTMask(value));
    }

    private static String convertNetmaskToCIDR(InetAddress netmask){
        byte[] netmaskBytes = netmask.getAddress();
        int cidr = 0;
        boolean zero = false;
        for(byte b : netmaskBytes){
            int mask = 0x80;
            for(int i = 0; i < 8; i++){
                int result = b & mask;
                if(result == 0){
                    zero = true;
                }else if(zero){
                    throw new IllegalArgumentException("Invalid netmask.");
                } else {
                    cidr++;
                }
                mask >>>= 1;
            }
        }
        return Integer.toString(cidr);
    }

    private static String convertCidrTMask(String cidr) throws AddressStringException {
        IPAddressString addressString = new IPAddressString("1/"+ cidr);
        IPAddress address = addressString.toAddress();
        Integer prefix = address.getNetworkPrefixLength();
        IPAddress mask = address.getNetwork().getNetworkMask(prefix, false);
        return mask.toString();
    }

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
