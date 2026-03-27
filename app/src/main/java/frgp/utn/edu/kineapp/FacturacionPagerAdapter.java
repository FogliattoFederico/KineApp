package frgp.utn.edu.kineapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FacturacionPagerAdapter extends FragmentStateAdapter {

    public FacturacionPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new FacturasEmitidasFragment();
            case 1: return new RemitosFragment();
            default: return new PagosColegioFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}