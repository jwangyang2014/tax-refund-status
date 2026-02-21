import { useCallback, useEffect, useState } from "react";
import { logout, me } from "./api/authApi";
import ErrorBanner from "./components/ErrorBanner";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";

type Screen = 'login' | 'register' | 'dashboard';

export default function App() {
  const [screen, setScreen] = useState<Screen>('login');
  const [error, setError] = useState<string | null>(null);

  // ✅ stable handlers so children don't re-run effects endlessly
  const handleError = useCallback((message: string) => {
    setError(message);
  }, []);

  const goLogin = useCallback(() => {
    setError(null);
    setScreen('login');
  }, []);

  const goRegister = useCallback(() => {
    setError(null);
    setScreen('register');
  }, []);

  const goDashboard = useCallback(() => {
    setError(null);
    setScreen('dashboard');
  }, []);

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

  const doLogout = useCallback(async () => {
    try {
      await logout();
    } finally {
      setError(null);
      setScreen('login');
    }
  }, []);

  return (
    <div style={{ padding: 16, fontFamily: 'sans-serif' }}>
      <h2>TurboTax Refund Status (Demo)</h2>
      <ErrorBanner message={error} />

      {screen === 'login' ? (
        <LoginPage
          onSuccess={goDashboard}
          onRegister={goRegister}
          onError={handleError}
        />
      ) : null}

      {screen === 'register' ? (
        <RegisterPage
          onSuccess={goLogin}
          onBack={goLogin}
          onError={handleError}
        />
      ) : null}

      {screen === 'dashboard' ? (
        <DashboardPage
          onLogout={doLogout}
          onError={handleError}
        />
      ) : null}
    </div>
  );
}