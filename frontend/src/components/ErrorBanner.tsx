import React from 'react';

export default function ErrorBanner({ message }: { message: string | null }) {
  if (!message) return null;

  return (
    <div style={{ border: '1px solid #c00', padding: 8, marginBottom: 12}}>
      <strong>Error:</strong>{ message }
    </div>
  );
}