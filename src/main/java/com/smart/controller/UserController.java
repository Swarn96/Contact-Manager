package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;


//security context holder se check krskte login hai ki nhi


@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal)
	{
		String userName = principal.getName();
		System.out.println("UserName" +userName);
		
		User user = userRepository.getUserByUserName(userName);
		System.out.println(user);
		
		model.addAttribute("user",user);
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	// open addform handler	
	@GetMapping("/add_contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","Add-Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
     // processing add contact handler
	
	@PostMapping("/process_contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session) {
		
		try {
		String name = principal.getName();
		User userByUserName = this.userRepository.getUserByUserName(name);
		
		contact.setUser(userByUserName);
		userByUserName.getContacts().add(contact);
		
		// processing and uploading file.
		if(file.isEmpty()) {
			System.out.println("file is empty");
			contact.setImage("contact.png");
		}else
		{
			//fill the file to static folder and update name to contact
			contact.setImage(file.getOriginalFilename());
			
			File file2 = new ClassPathResource("static/image").getFile();
			Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
		     Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
		 System.out.println("image is uploaded");
		}
		
		this.userRepository.save(userByUserName);
		
		System.out.println(contact);
		System.out.println("added to database");
		
		// successfully added contact
		// success message
		
		session.setAttribute("message", new Message("Your contact is addedd !!","alert-success"));
		
		
		}catch (Exception e) {
			System.out.println("Error "+e.getMessage());
			e.printStackTrace(); 
			// error message if contact not added
			session.setAttribute("message", new Message("Something went wrong !!","alert-danger"));
			
		}
		return "normal/add_contact_form";
	}
	
	//handler for viewing the all contacts
	
	//per page 5contact bhejna hai
	// current page= 0[page
	
	@GetMapping("/show_contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model,Principal principal) {
		
		model.addAttribute("title","View-Contacts");
		
		//contacts list ko send krna hai database se nikal kr
		
	String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		int id = user.getId();
		
		Pageable pageable = PageRequest.of(page,2);
	      Page<Contact> contacts=this.contactRepository.findContactsByUser(id, pageable);
		
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);// variable-page aaya hai controller se 
		model.addAttribute("totalpages",contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
	
	
	// showing specific contact details
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
		
		System.out.println(cId);
		Optional<Contact> contact = this.contactRepository.findById(cId);
		Contact userContact = contact.get();
		
		
		//principal use kr rhe bcz particular user sirf apna contacts dekh ske,,,kisi aur ka nhi url me change krke
		String userName = principal.getName();
		User currentUser= this.userRepository.getUserByUserName(userName);
		
		if(currentUser.getId()==userContact.getUser().getId())
		{
			model.addAttribute("title",userContact.getName());
			model.addAttribute("userContact",userContact);
		}
		
		
		return "normal/contact_detail";
	}
	
	// delete specific contact
	
//	@GetMapping("/delete/{cid}")
//	public String deleteContact(@PathVariable("cid") Integer cId, Model model,Principal principal,HttpSession session) {
//		
//		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
//		Contact contact = contactOptional.get();
//		
//		// check krna hoga ki same user hai ki nhi
//		
//		String userName = principal.getName();
//		User currentUser= this.userRepository.getUserByUserName(userName);
//		
//		if(currentUser.getId()==contact.getUser().getId())
//		{
//			
//			
////			contact.setUser(null);//unlink the contact user from main user table
//			
//			//list se contact remove tb hoga jb mere pass object matching wali method hoga contact entity mei.
//			currentUser.getContacts().remove(contact);
//			this.userRepository.save(currentUser);
//			
////			this.contactRepository.delete(contact);
//			session.setAttribute("message",new Message("Contact successfully deleted..","success"));
//		}
//		
//		return "redirect:/user/show_contacts/0";
//	}
	
	
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, Principal principal, HttpSession session) {

	    Optional<Contact> contactOptional = this.contactRepository.findById(cId);
	    if (contactOptional.isPresent()) {
	        Contact contact = contactOptional.get();

	        String userName = principal.getName();
	        User currentUser = this.userRepository.getUserByUserName(userName);

	        if (currentUser.getId() == contact.getUser().getId()) {
	            // Remove the contact from the current user's list of contacts
	            currentUser.getContacts().remove(contact);
	            // Unlink the contact user from the main user table
	            contact.setUser(null);
	            // Delete the contact from the database
	            this.contactRepository.delete(contact);

	            session.setAttribute("message", new Message("Contact successfully deleted.", "alert-success"));
	        }
	    }

	    return "redirect:/user/show_contacts/0";
	}
	
	
	// open update form handler
	
	@PostMapping("/update_contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model)
	{
		
		model.addAttribute("title","Update-Form");
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		model.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	// process for update contact handler
	@PostMapping("/process_update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal principal)
	{
		System.out.println(contact.getcId());
		System.out.println(contact.getName());
		
		
		//old contact details nikalna hogaimage ke liye if someone not uploaded the new image then 
		// we show the old image ..bcz it is update field.
		
		Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();
		
		try {
			
			if(!file.isEmpty())
			{
				// new image override krna hoga
				
				//delete old photo means from the static folder not from database.
				
				File deleteFile = new ClassPathResource("static/image").getFile();
				//upar wale se sare images mil jayenge jo bhi us folder me hai sare aa jayenge deletefile mei
				// but apne ko append krna hoga usme old file name.
				File file3=new File(deleteFile,oldContactDetails.getImage());
				file3.delete();
				
				// update new photo

				File file2 = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
			     Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
			}else {
				
				contact.setImage(oldContactDetails.getImage());
			}
			
			// user_id jo table me hai usi ko update krna hai to phle vo user
			//ko nikalna hoga then uska user_id null show krega direct krne se toh hme null nhi rkhne ke liye 
			// contact.setUser() method se uska user_id set krna hoga then
			// this.contactRepository.save() method se baki detail save kr denge, means update kr denge.
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact is updated..","alert-success"));
			
		}catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	// Your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title","Your-Profile");
		
		return "normal/profile";
	}
	
	//open setting handler
	
	@GetMapping("/settings")
	public String openSettings()
	{
		
		return "normal/settings";
	}
	
	// Change Password handler
	
	@PostMapping("/change_password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) 
	{
		System.out.println("oldPassword"+oldPassword);
		System.out.println("newPassword"+newPassword);
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword()))
		{
			// change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			
			session.setAttribute("message", new Message("Your Password is changed....","alert-success"));
		
		}else {
			// error
			
			session.setAttribute("message", new Message("Your Old Password doesn't match....","alert-danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
	
	
}
