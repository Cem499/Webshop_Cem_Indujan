import './App.css';
import { Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Layout from './pages/Layout';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Forbidden from './pages/Forbidden';
import KategorienList from './components/KategorienList';
import KategorienForm from './components/KategorienForm';
import ProdukteList from './components/ProdukteList';
import ProdukteForm from './components/ProdukteForm';
import Warenkorb from './pages/Warenkorb';
import Bestellungen from './pages/Bestellungen';
import BestellungenList from './components/BestellungenList';
import BestellungenForm from './components/BestellungenForm';
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    // AuthProvider muss innerhalb von BrowserRouter (in main.jsx) liegen,
    // damit useNavigate() im AuthContext funktioniert.
    <AuthProvider>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Home />} />

          {/* Öffentliche Seiten */}
          <Route path="login" element={<Login />} />
          <Route path="register" element={<Register />} />
          <Route path="forbidden" element={<Forbidden />} />
          <Route path="produkte" element={<ProdukteList />} />

          {/* Alle eingeloggten User: ADMIN und KUNDE */}
          <Route path="warenkorb" element={
            <ProtectedRoute allowedRoles={["ADMIN", "KUNDE"]}>
              <Warenkorb />
            </ProtectedRoute>
          } />
          <Route path="bestellungen" element={
            <ProtectedRoute allowedRoles={["KUNDE"]}>
              <Bestellungen />
            </ProtectedRoute>
          } />

          {/* Nur ADMIN */}
          <Route path="admin/produkte" element={
            <ProtectedRoute requiredRole="ADMIN">
              <ProdukteForm />
            </ProtectedRoute>
          } />
          <Route path="admin/kategorien" element={
            <ProtectedRoute requiredRole="ADMIN">
              <KategorienForm />
            </ProtectedRoute>
          } />
          <Route path="admin/bestellungen" element={
            <ProtectedRoute requiredRole="ADMIN">
              <BestellungenList />
            </ProtectedRoute>
          } />
          <Route path="admin/meine-bestellungen" element={
            <ProtectedRoute requiredRole="ADMIN">
              <Bestellungen />
            </ProtectedRoute>
          } />

          {/* Bestehende Routen bleiben erhalten */}
          <Route path="kategorien" element={<KategorienList />} />
          <Route path="new-kategorie" element={<KategorienForm />} />
          <Route path="edit-kategorie/:id" element={<KategorienForm />} />
          <Route path="new-produkt" element={<ProdukteForm />} />
          <Route path="edit-produkt/:id" element={<ProdukteForm />} />
          <Route path="edit-bestellung/:id" element={<BestellungenForm />} />
        </Route>
      </Routes>
    </AuthProvider>
  );
}

export default App;
