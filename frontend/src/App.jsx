import { useEffect, useState } from 'react';
import { request } from './api.js';

export default function App() {
  const [status, setStatus] = useState('checking');
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setStatus('checking');
    request('/actuator/health', { signal: controller.signal })
      .then(body => setStatus(body.status === 'UP' ? 'connected' : 'unavailable'))
      .catch(() => {
        if (!controller.signal.aborted) setStatus('unavailable');
      });
    return () => controller.abort();
  }, [revision]);

  return (
    <main>
      <p className="eyebrow">ELECTROSTOCK · INITIAL SETUP</p>
      <h1>Electronics inventory & procurement</h1>
      <p className="description">A full-stack project built with React and Spring Boot.</p>
      <section className="panel" aria-labelledby="setup-heading">
        <h2 id="setup-heading">Development foundation</h2>
        <p>React frontend is running.</p>
        <p role="status" className={`status ${status}`}>
          {status === 'checking' && 'Checking Spring Boot connection…'}
          {status === 'connected' && 'Spring Boot backend connected.'}
          {status === 'unavailable' && 'Backend unavailable. Start Spring Boot on port 8080.'}
        </p>
        <button type="button" onClick={() => setRevision(value => value + 1)} disabled={status === 'checking'}>
          Check connection
        </button>
      </section>
      <footer>Basic setup only · Feature development follows the team plan.</footer>
    </main>
  );
}
