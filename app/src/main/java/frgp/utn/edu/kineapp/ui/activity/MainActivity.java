package frgp.utn.edu.kineapp.ui.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import frgp.utn.edu.kineapp.ui.fragment.AgendaFragment;
import frgp.utn.edu.kineapp.ui.fragment.FacturacionFragment;
import frgp.utn.edu.kineapp.ui.fragment.PacientesFragment;
import frgp.utn.edu.kineapp.ui.fragment.PerfilFragment;
import frgp.utn.edu.kineapp.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#1565C0"));
        getWindow().getDecorView().setSystemUiVisibility(0);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            cargarFragment(new AgendaFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_agenda) {
                cargarFragment(new AgendaFragment());
            } else if (id == R.id.nav_pacientes) {
                cargarFragment(new PacientesFragment());
            } else if (id == R.id.nav_facturacion) {
                cargarFragment(new FacturacionFragment());
            } else if (id == R.id.nav_perfil) {
                cargarFragment(new PerfilFragment());
            }
            return true;
        });
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}