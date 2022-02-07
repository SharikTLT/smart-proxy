package ru.shariktlt.smart.admin;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.shariktlt.smart.proxy.ServerRecord;
import ru.shariktlt.smart.proxy.ServersRegistry;

@Controller
public class AdminController {

    @Autowired
    @Qualifier("SmartProxyPort")
    private int smartProxyPort;

    @Autowired
    private ServersRegistry registry;

    @GetMapping("/")
    public String dashboard(Model model){
        model.addAttribute("proxyPort", smartProxyPort);
        model.addAttribute("servers", registry.getServers());
        return "index";
    }

    @GetMapping("/view")
    public String view(@RequestParam("provider") String provider, Model model){
        model.addAttribute("serverUrls", registry.getServerUrls(new ServerRecord(provider)));
        model.addAttribute("provider", provider);
        return "view";
    }

    @PostMapping("/")
    public String dashboard(@RequestParam("provider") String providers){
        for (String provider : providers.split("\\|")) {
            if(provider == null || provider.isEmpty()){
                continue;
            }
            registry.register(new ServerRecord(provider));
        }
        return "redirect:/?added=ok";
    }


    @GetMapping("/remove")
    public String remove(@RequestParam("provider") String provider){
        registry.unregister(new ServerRecord(provider));
        return "redirect:/?removed="+provider;
    }
}
