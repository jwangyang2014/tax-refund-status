import { useEffect, useState } from "react";
import { logout, me } from "./api/authApi";
import ErrorBanner from "./components/ErrorBanner";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";

type Screen = 'login' | 'register' | 'dashboard';

export default function App() {
  const [screen, setScreen] = useState<Screen>('login');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        await me();
        setScreen('dashboard');
      } catch {
        setScreen('login');
      }
    })();
  }, []);

  async function doLogout() {
    try {
      await logout();
    } finally {
      setScreen('login');
    }
  }

  return (
    <div style={{ padding: 16, fontFamily: 'sans-serif'}}>
      <h2>TurboTax Refund Status (Demo)</h2>
      <ErrorBanner message={error} />

      {screen === 'login' ? (
        <LoginPage
          onSuccess={() => {
            setError(null);
            setScreen('dashboard');
          }}
          onRegister={() => {
            setError(null);
            setScreen('register');
          }}
          onError={(message) => setError(message)}
        />
      ) : null}

      {screen === 'register' ? (
        <RegisterPage
          onSuccess={() => {
            setError(null);
            setScreen('login');
          }}
          onBack={() => {
            setError(null);
            setScreen('login');
          }}
          onError={(message) => setError(message)}
        />
      ) : null}

      {screen === 'dashboard' ? (
        <DashboardPage
          onLogout={doLogout}
          onError={(message) => setError(message)}
        />
      ) : null}
    </div>
  );
}