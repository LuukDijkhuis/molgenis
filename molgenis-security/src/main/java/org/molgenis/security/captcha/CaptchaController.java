package org.molgenis.security.captcha;

import java.awt.image.BufferedImage;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/captcha")
public class CaptchaController {
  private static final int CAPTCHA_WIDTH = 220;
  private static final int CAPTCHA_HEIGHT = 50;

  @Autowired private CaptchaService captchaService;

  @GetMapping(produces = "image/jpeg")
  @ResponseBody
  public BufferedImage getCaptcha() {
    return captchaService.createCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT);
  }

  @PostMapping
  @ResponseBody
  public Boolean validateCaptcha(@Valid @RequestBody CaptchaRequest captchaRequest)
      throws CaptchaException {
    return captchaService.validateCaptcha(captchaRequest.getCaptcha());
  }

  // Spring's FormHttpMessageConverter cannot bind target classes (as ModelAttribute can)
  @PostMapping(headers = "Content-Type=application/x-www-form-urlencoded")
  @ResponseBody
  public Boolean validateCaptchaFromForm(@Valid @ModelAttribute CaptchaRequest captchaRequest)
      throws CaptchaException {
    return captchaService.validateCaptcha(captchaRequest.getCaptcha());
  }
}
