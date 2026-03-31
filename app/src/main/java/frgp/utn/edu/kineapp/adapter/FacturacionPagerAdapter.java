package frgp.utn.edu.kineapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import frgp.utn.edu.kineapp.ui.fragment.FacturasEmitidasFragment;
import frgp.utn.edu.kineapp.ui.fragment.OrdenesVinculacionFragment;
import frgp.utn.edu.kineapp.ui.fragment.PagosColegioFragment;
import frgp.utn.edu.kineapp.ui.fragment.RemitosFragment;

public class FacturacionPagerAdapter extends FragmentStateAdapter {

    public FacturacionPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new RemitosFragment();
            case 1: return new OrdenesVinculacionFragment();
            case 2: return new PagosColegioFragment();
            default: return new FacturasEmitidasFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}