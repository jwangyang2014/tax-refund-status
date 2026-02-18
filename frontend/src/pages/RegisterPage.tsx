import React, { useState } from 'react';
import { register } from '../api/authApi';
import { errorMessage } from '../utils';

export default function RegisterPage(props: {
  onSuccess: () => void;
  onBack: () => void;
  onError: (msg: string) => void;
}) {
  const [email, setEmail] = useState('yang@example.com');
  const [password, setPassword] = useState('Password@123#!');
  const [loading, setLoading] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);

    try {
      await register(email, password);
      props.onSuccess();
    } catch(err: unknown) {
      props.onError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h3>Register</h3>
      <form onSubmit={submit} style={{ display: 'grid', gap: 8, maxWidth: 420}}>
        <input value={email} onChange={(e) => setEmail(e.target.value)}/>        
        <input 
          value={password} 
          onChange={(e) => setPassword(e.target.value)}
          placeholder='password'
          type='password'
        />
        <button disabled={loading}>{loading ? '...' : 'Register'}</button>
      </form>
      <p style={{ marginTop: 12 }}>
        <button onClick={props.onBack}>Back</button>
      </p>
    </div>
  );
}
