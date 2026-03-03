import './App.css';
import { Routes, Route } from 'react-router-dom';
import Layout from './pages/Layout';
import Home from './pages/Home';
import KategorienList from './components/KategorienList';
import KategorienForm from './components/KategorienForm';
import ProdukteList from './components/ProdukteList';
import ProdukteForm from './components/ProdukteForm';
import Warenkorb from './pages/Warenkorb';
import Bestellungen from './pages/Bestellungen';
import BestellungenForm from './components/BestellungenForm';

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="kategorien" element={<KategorienList />} />
        <Route path="new-kategorie" element={<KategorienForm />} />
        <Route path="edit-kategorie/:id" element={<KategorienForm />} />
        <Route path="produkte" element={<ProdukteList />} />
        <Route path="new-produkt" element={<ProdukteForm />} />
        <Route path="edit-produkt/:id" element={<ProdukteForm />} />
        <Route path="warenkorb" element={<Warenkorb />} />
        <Route path="bestellungen" element={<Bestellungen />} />
        <Route path="edit-bestellung/:id" element={<BestellungenForm />} />
      </Route>
    </Routes>
  );
}

export default App;