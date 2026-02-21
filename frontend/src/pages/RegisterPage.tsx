import React, { useMemo, useState } from 'react';
import { register } from '../api/authApi';
import { errorMessage } from '../utils';

type FieldProps = {
  id: string;
  label: string;
  required?: boolean;
  error?: string | null;
  children: React.ReactNode;
};

function Field(props: FieldProps) {
  return (
    <div style={{ display: 'grid', gridTemplateColumns: '160px 1fr', gap: 8 }}>
      <label htmlFor={props.id} style={{ fontWeight: 500, paddingTop: 6 }}>
        {props.label} {props.required ? <span>*</span> : null}
      </label>

      <div style={{ display: 'grid', gap: 4 }}>
        {props.children}
        {props.error ? (
          <div style={{ color: 'crimson', fontSize: 13 }}>{props.error}</div>
        ) : null}
      </div>
    </div>
  );
}

export default function RegisterPage(props: {
  onSuccess: () => void;
  onBack: () => void;
  onError: (msg: string) => void;
}) {
  const [email, setEmail] = useState('yang@example.com');

  const [password, setPassword] = useState('');
  const [repeatPassword, setRepeatPassword] = useState('');

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');

  const [address, setAddress] = useState('');
  const [city, setCity] = useState('');
  const [state, setState] = useState('');
  const [phone, setPhone] = useState('');

  const [loading, setLoading] = useState(false);

  const required = (v: string) => (v.trim() ? null : 'Required');

  // -------- validation --------
  const passwordLengthError =
    password.length > 0 && password.length < 10
      ? 'Password must be at least 10 characters'
      : null;

  const passwordMatchError =
    repeatPassword.length > 0 && password !== repeatPassword
      ? 'Passwords do not match'
      : null;

  const emailError = required(email);
  const firstNameError = required(firstName);
  const lastNameError = required(lastName);
  const cityError = required(city);
  const stateError = required(state);

  const canSubmit = useMemo(() => {
    return (
      !loading &&
      !emailError &&
      !firstNameError &&
      !lastNameError &&
      !cityError &&
      !stateError &&
      !passwordLengthError &&
      !passwordMatchError &&
      password.trim().length > 0 &&
      repeatPassword.trim().length > 0
    );
  }, [
    loading,
    emailError,
    firstNameError,
    lastNameError,
    cityError,
    stateError,
    passwordLengthError,
    passwordMatchError,
    password,
    repeatPassword
  ]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();

    // show errors (by preventing submit); UI already renders inline errors
    if (!canSubmit) return;

    setLoading(true);
    try {
      await register({
        email: email.trim(),
        password,
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        address: address.trim() ? address.trim() : null,
        city: city.trim(),
        state: state.trim().toUpperCase(),
        phone: phone.trim() ? phone.trim() : null
      });

      props.onSuccess();
    } catch (err: unknown) {
      props.onError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <h3>Register</h3>

      <form onSubmit={submit} style={{ display: 'grid', gap: 12, maxWidth: 600 }}>
        <Field id="reg-email" label="Email" required error={emailError}>
          <input
            id="reg-email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="email"
          />
        </Field>

        <Field id="reg-password" label="Password" required error={passwordLengthError}>
          <input
            id="reg-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
          />
        </Field>

        <Field id="reg-repeat-password" label="Repeat Password" required error={passwordMatchError}>
          <input
            id="reg-repeat-password"
            type="password"
            value={repeatPassword}
            onChange={(e) => setRepeatPassword(e.target.value)}
            autoComplete="new-password"
          />
        </Field>

        <Field id="reg-first-name" label="First Name" required error={firstNameError}>
          <input
            id="reg-first-name"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            autoComplete="given-name"
          />
        </Field>

        <Field id="reg-last-name" label="Last Name" required error={lastNameError}>
          <input
            id="reg-last-name"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            autoComplete="family-name"
          />
        </Field>

        <Field id="reg-address" label="Address">
          <input
            id="reg-address"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            autoComplete="street-address"
          />
        </Field>

        <Field id="reg-city" label="City" required error={cityError}>
          <input
            id="reg-city"
            value={city}
            onChange={(e) => setCity(e.target.value)}
            autoComplete="address-level2"
          />
        </Field>

        <Field id="reg-state" label="State" required error={stateError}>
          <input
            id="reg-state"
            value={state}
            onChange={(e) => setState(e.target.value.toUpperCase())}
            maxLength={2}
            autoComplete="address-level1"
          />
        </Field>

        <Field id="reg-phone" label="Phone">
          <input
            id="reg-phone"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            autoComplete="tel"
          />
        </Field>

        <div style={{ display: 'flex', gap: 12, marginTop: 8 }}>
          <button disabled={!canSubmit}>{loading ? '...' : 'Register'}</button>

          <button type="button" onClick={props.onBack}>
            Back
          </button>
        </div>
      </form>
    </div>
  );
}