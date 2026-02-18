import React, { useState } from 'react';
import { login } from '../api/authApi';
import { errorMessage } from '../utils';

export default function LoginPage(props: {
  onSuccess: () => void;
  onRegister: () => void;
  onError: (msg: string) => void
}) {
  const [email, setEmail] = useState('yang@example.com');
  const [password, setPassword] = useState('Password@123#!');
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);

    try {
      await login(email, password);
      props.onSuccess();
    } catch (err: unknown) {
      props.onError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h3>Login</h3>
      <form onSubmit={submit} style={{ display: 'grid', gap: 8, maxWidth: 420 }}>
        <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder='email'/>
        <input
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder='password'
          type='password'
        />
        <button disabled={loading}>{loading? '...' : 'Login'}</button>
      </form>
      <p style={{marginTop: 12}}>
        No account? <button onClick={props.onRegister}>Register</button>
      </p>
    </div>
  );
}