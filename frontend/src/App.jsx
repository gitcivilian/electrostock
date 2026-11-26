import { useCallback, useEffect, useState } from 'react';
import { listCategories, request } from './api.js';
import CategoryCatalog from './components/CategoryCatalog.jsx';
import ComponentCatalog from './components/ComponentCatalog.jsx';
import { ArchiveIcon, ChipIcon, LayersIcon, RefreshIcon } from './components/Icons.jsx';

export default function App() {
  const [healthStatus, setHealthStatus] = useState('checking');
  const [revision, setRevision] = useState(0);
  const [activeTab, setActiveTab] = useState('components');
  const [categories, setCategories] = useState([]);
  const [catalogVersion, setCatalogVersion] = useState(0);

  const refreshCategories = useCallback(async () => {
    try {
      const data = await listCategories({ status: 'ALL' });
      setCategories(data);
    } catch {
      // Errors are surfaced by the active catalog view and health banner.
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    setHealthStatus('checking');
    request('/actuator/health', { signal: controller.signal })
      .then(body => {
        const up = body?.status === 'UP';
        setHealthStatus(up ? 'connected' : 'unavailable');
        if (up) {
          refreshCategories().then(() => setCatalogVersion(value => value + 1));
        }
      })
      .catch(() => {
        if (!controller.signal.aborted) {
          setHealthStatus('unavailable');
        }
      });
    return () => controller.abort();
  }, [revision, refreshCategories]);

  async function handleCatalogMutation() {
    await refreshCategories();
    setCatalogVersion(value => value + 1);
  }

  const activeCategoryCount = categories.filter(category => category.active).length;
  const totalComponentCount = categories.reduce((sum, category) => sum + category.componentCount, 0);
  const activeComponentCount = categories.reduce((sum, category) => sum + category.activeComponentCount, 0);
  const inactiveCount =
    (categories.length - activeCategoryCount) + (totalComponentCount - activeComponentCount);

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar-inner">
          <div className="brand">
            <div className="brand-mark">
              <ChipIcon />
            </div>
            <div>
              <h1 className="brand-title">ElectroStock</h1>
              <p className="brand-subtitle">Electronics Inventory &amp; Component Catalog</p>
            </div>
          </div>

          <div className="topbar-actions">
            <nav className="nav-tabs" aria-label="Catalog sections">
              <button
                type="button"
                className={`nav-tab ${activeTab === 'components' ? 'is-active' : ''}`}
                onClick={() => setActiveTab('components')}
              >
                <span>Components</span>
                <span className="tab-count">{totalComponentCount}</span>
              </button>
              <button
                type="button"
                className={`nav-tab ${activeTab === 'categories' ? 'is-active' : ''}`}
                onClick={() => setActiveTab('categories')}
              >
                <span>Categories</span>
                <span className="tab-count">{categories.length}</span>
              </button>
            </nav>

            <div className="health-pill-group">
              <p role="status" className={`health-pill ${healthStatus}`}>
                <span className="health-dot" aria-hidden="true" />
                {healthStatus === 'checking' && 'Checking MongoDB…'}
                {healthStatus === 'connected' && 'MongoDB Connected'}
                {healthStatus === 'unavailable' && 'Backend / DB Offline'}
              </p>
              <button
                type="button"
                className="secondary icon-btn"
                onClick={() => setRevision(value => value + 1)}
                disabled={healthStatus === 'checking'}
                title="Re-check backend & MongoDB connection"
                aria-label="Re-check connection"
              >
                <RefreshIcon />
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="workspace">
        <section className="kpi-grid" aria-label="Catalog overview">
          <article className="kpi-card">
            <div>
              <div className="kpi-label">Active Components</div>
              <div className="kpi-value">{activeComponentCount}</div>
              <div className="kpi-meta">{totalComponentCount} total registered parts</div>
            </div>
            <div className="kpi-icon">
              <ChipIcon />
            </div>
          </article>

          <article className="kpi-card">
            <div>
              <div className="kpi-label">Active Categories</div>
              <div className="kpi-value">{activeCategoryCount}</div>
              <div className="kpi-meta">{categories.length} total component families</div>
            </div>
            <div className="kpi-icon">
              <LayersIcon />
            </div>
          </article>

          <article className="kpi-card">
            <div>
              <div className="kpi-label">Inactive / Archived</div>
              <div className="kpi-value">{inactiveCount}</div>
              <div className="kpi-meta">Soft-deactivated for audit history</div>
            </div>
            <div className="kpi-icon muted-icon">
              <ArchiveIcon />
            </div>
          </article>
        </section>

        {activeTab === 'components' ? (
          <ComponentCatalog
            key={`components-${catalogVersion}`}
            categories={categories}
            onComponentsChanged={refreshCategories}
            onSwitchToCategories={() => setActiveTab('categories')}
          />
        ) : (
          <CategoryCatalog onCategoriesChanged={handleCatalogMutation} />
        )}
      </main>

      <footer className="app-footer">
        <span>ElectroStock · Member 2 Catalog Workspace</span>
        <span>Spring Boot · MongoDB Atlas · Soft-deactivation preserves audit integrity</span>
      </footer>
    </div>
  );
}
