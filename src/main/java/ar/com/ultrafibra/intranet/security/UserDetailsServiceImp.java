package ar.com.ultrafibra.intranet.security;

import ar.com.ultrafibra.intranet.dao.iUserDao;
import ar.com.ultrafibra.intranet.entities.Role;
import ar.com.ultrafibra.intranet.entities.User;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
public class UserDetailsServiceImp implements UserDetailsService {

    @Autowired
    private iUserDao userDao;

   @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User usuario = userDao.findByUsername(username);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role rol : usuario.getRoles()) {
            System.out.println("ROLE = " + rol.getRole());
            authorities.add(new SimpleGrantedAuthority(rol.getRole()));
        }
        return new org.springframework.security.core.userdetails.User(
            usuario.getUsername(), usuario.getPassword(), authorities);
    }

}
