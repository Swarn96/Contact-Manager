package com.smart.controller;

import java.security.SecureRandom;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.services.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	//Random random = new Random(1000);
	
	long seed = System.currentTimeMillis() + new SecureRandom().nextLong();
	Random random = new Random(seed);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	
	
	//open email handler
	
	@GetMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	// otp controller
	
	@PostMapping("/send_otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session)
	{
		System.out.println("Email "+email);
		// generating otp of 4 digit
		
		
		int otp = random.nextInt(999999);
		System.out.println("OTP"+otp);
		
		//write code for send otp to email
		
		String subject="OTP From Smart Contact Manager Application";
		String message=""
				+ "<div style='border:1px solid #e2e2e2; padding:20px'>"
				+ "<h1>"
				+ "OTP is "
				+ "<b>"+otp
				+ "</n>"
				+ "</h1>"
				+ "</div>";
				
				
		String to = email;
		boolean flag =this.emailService.sendEmail(subject, message,to);
		
		if(flag)
		{
			session.setAttribute("backend_otp",otp);
			session.setAttribute("email",email);
			return "verify_OTP";
		}
		else {
			
			session.setAttribute("message", "Check Your Email-Id !!");
			
			return "forgot_email_form";
		}
		
		
	}
	
	//verify otp
	@PostMapping("/verify_otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session)
	{
		int backend_otp=(int) session.getAttribute("backend_otp");
		String email=(String) session.getAttribute("email");
		
		if(backend_otp==otp)
		{
			
			//password change form
			/* user fetching by email */
			User user= this.userRepository.getUserByUserName(email);
			
			if(user==null)
			{
				//send error message
				session.setAttribute("message", "User does not exist with this email !!");
				
				return "forgot_email_form";
				
			}else {
				
				//password change
				
			}
			
			return "password_change_form";
			
		}else {
			
			session.setAttribute("message", "You have entered wrong otp !!");
			return "verify_OTP";
			
		}
		
	}
	
	//change password
	@PostMapping("/change_password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session)
	{
		String email=(String) session.getAttribute("email");
	
	User user = this.userRepository.getUserByUserName(email);
	user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
	this.userRepository.save(user);
	
	
	return "redirect:/signin?change=paasword changed successfuly..";
	}
}
