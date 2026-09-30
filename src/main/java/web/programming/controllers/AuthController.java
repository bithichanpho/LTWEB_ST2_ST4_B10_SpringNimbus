package web.programming.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class AuthController {

	// Vào trang gốc "/" thì chuyển thẳng đến trang đăng nhập
	@GetMapping
	public String root() {
		return "redirect:/login";
	}

	@GetMapping("login")
	public String index() {
		return "login";
	}

	@GetMapping("user/profile")
	public String profile() {
		return "profile";
	}

}
