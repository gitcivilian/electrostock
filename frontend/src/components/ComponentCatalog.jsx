import { useEffect, useState } from 'react';
import {
  activateComponent,
  createComponent,
  deactivateComponent,
  listComponents,
  updateComponent,
} from '../api.js';
import { ChipIcon, CloseIcon, PinIcon, PlusIcon, SearchIcon } from './Icons.jsx';

const UNIT_SUGGESTIONS = ['pcs', 'reels', 'tubes', 'trays', 'packs', 'meters', 'kits'];

const STATUS_OPTIONS = [
  { value: 'ALL', label: 'All' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
];

const EMPTY_FORM = {
  partNumber: '',
  name: '',
  categoryId: '',
  manufacturer: '',
  packageType: '',
  unit: 'pcs',
  storageLocation: '',
  reorderThreshold: '10',
  description: '',
  specs: [{ key: '', value: '' }],
};

export default function ComponentCatalog({ categories, onComponentsChanged, onSwitchToCategories }) {
  const [components, setComponents] = useState([]);
  const [search, setSearch] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [busyId, setBusyId] = useState(null);

  const activeCategories = categories.filter(category => category.active);

  async function loadComponents(
    currentSearch = search,
    currentCategory = categoryFilter,
    currentStatus = statusFilter
  ) {
    setLoading(true);
    setError('');
    try {
      const data = await listComponents({
        search: currentSearch,
        categoryId: currentCategory,
        status: currentStatus,
      });
      setComponents(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = setTimeout(() => {
      loadComponents(search, categoryFilter, statusFilter);
    }, 180);
    return () => clearTimeout(timer);
  }, [search, categoryFilter, statusFilter]);

  useEffect(() => {
    if (!formOpen) return undefined;
    function onKeyDown(event) {
      if (event.key === 'Escape' && !submitting) {
        closeForm();
      }
    }
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [formOpen, submitting]);

  function openCreateForm() {
    setEditingId(null);
    setForm({
      ...EMPTY_FORM,
      categoryId: activeCategories[0]?.id || '',
      specs: [{ key: '', value: '' }],
    });
    setError('');
    setNotice('');
    setFormOpen(true);
  }

  function openEditForm(component) {
    const specEntries = Object.entries(component.specifications || {});
    setEditingId(component.id);
    setForm({
      partNumber: component.partNumber,
      name: component.name,
      categoryId: component.categoryId,
      manufacturer: component.manufacturer || '',
      packageType: component.packageType || '',
      unit: component.unit || 'pcs',
      storageLocation: component.storageLocation || '',
      reorderThreshold: String(component.reorderThreshold ?? 0),
      description: component.description || '',
      specs: specEntries.length > 0
        ? specEntries.map(([key, value]) => ({ key, value }))
        : [{ key: '', value: '' }],
    });
    setError('');
    setNotice('');
    setFormOpen(true);
  }

  function closeForm() {
    setFormOpen(false);
    setEditingId(null);
    setForm(EMPTY_FORM);
  }

  function updateSpecRow(index, field, value) {
    setForm(prev => ({
      ...prev,
      specs: prev.specs.map((row, rowIndex) => (rowIndex === index ? { ...row, [field]: value } : row)),
    }));
  }

  function addSpecRow() {
    setForm(prev => ({
      ...prev,
      specs: [...prev.specs, { key: '', value: '' }],
    }));
  }

  function removeSpecRow(index) {
    setForm(prev => {
      const next = prev.specs.filter((_, rowIndex) => rowIndex !== index);
      return { ...prev, specs: next.length > 0 ? next : [{ key: '', value: '' }] };
    });
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setNotice('');

    const specifications = {};
    const seenKeys = new Set();
    for (const row of form.specs) {
      const key = row.key.trim();
      const value = row.value.trim();
      if (!key && !value) {
        continue;
      }
      if (!key || !value) {
        setError('Each technical specification row must include both a name and a value.');
        return;
      }
      const lowerKey = key.toLowerCase();
      if (seenKeys.has(lowerKey)) {
        setError(`Duplicate technical specification name: "${key}".`);
        return;
      }
      seenKeys.add(lowerKey);
      specifications[key] = value;
    }

    const parsedThreshold = Number.parseInt(form.reorderThreshold, 10);
    if (Number.isNaN(parsedThreshold) || parsedThreshold < 0) {
      setError('Reorder threshold must be 0 or a positive whole number.');
      return;
    }

    setSubmitting(true);
    const payload = {
      partNumber: form.partNumber.trim(),
      name: form.name.trim(),
      categoryId: form.categoryId,
      manufacturer: form.manufacturer.trim(),
      packageType: form.packageType.trim(),
      unit: form.unit.trim(),
      storageLocation: form.storageLocation.trim(),
      reorderThreshold: parsedThreshold,
      specifications,
      description: form.description.trim(),
    };

    try {
      if (editingId) {
        await updateComponent(editingId, payload);
        setNotice(`Updated component "${payload.partNumber}".`);
      } else {
        await createComponent(payload);
        setNotice(`Created component "${payload.partNumber}".`);
      }
      closeForm();
      await loadComponents();
      await onComponentsChanged();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleToggleActive(component) {
    setBusyId(component.id);
    setError('');
    setNotice('');
    try {
      if (component.active) {
        await deactivateComponent(component.id);
        setNotice(`Deactivated component "${component.partNumber}".`);
      } else {
        await activateComponent(component.id);
        setNotice(`Reactivated component "${component.partNumber}".`);
      }
      await loadComponents();
      await onComponentsChanged();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyId(null);
    }
  }

  const selectableCategories = categories.filter(
    category => category.active || (editingId && category.id === form.categoryId)
  );
  const hasFilters = Boolean(search.trim()) || Boolean(categoryFilter) || statusFilter !== 'ALL';

  return (
    <section className="panel" aria-labelledby="components-heading">
      <div className="panel-header">
        <div>
          <h2 id="components-heading">Component Catalog</h2>
          <p>Track part numbers, technical specs, storage bins, units, and minimum reorder thresholds.</p>
        </div>
        <button type="button" onClick={openCreateForm} disabled={activeCategories.length === 0}>
          <PlusIcon />
          <span>New component</span>
        </button>
      </div>

      <div className="panel-body">
        {activeCategories.length === 0 && (
          <div className="alert info">
            <span>
              Create at least one active category before adding components.
            </span>
            <button type="button" className="secondary small" onClick={onSwitchToCategories}>
              Go to Categories
            </button>
          </div>
        )}

        <div className="toolbar">
          <div className="search-box">
            <span className="search-icon"><SearchIcon /></span>
            <label className="sr-only" htmlFor="component-search">Search components</label>
            <input
              id="component-search"
              type="search"
              value={search}
              onChange={event => setSearch(event.target.value)}
              placeholder="Search part, name, manufacturer, location, or spec…"
            />
            {search && (
              <button
                type="button"
                className="search-clear"
                onClick={() => setSearch('')}
                aria-label="Clear search"
              >
                <CloseIcon />
              </button>
            )}
          </div>

          <label className="filter-select">
            <span>Category</span>
            <select value={categoryFilter} onChange={event => setCategoryFilter(event.target.value)}>
              <option value="">All categories</option>
              {categories.map(category => (
                <option key={category.id} value={category.id}>
                  {category.code} — {category.name}
                  {!category.active ? ' (Inactive)' : ''}
                </option>
              ))}
            </select>
          </label>

          <div className="segmented" role="group" aria-label="Filter components by status">
            {STATUS_OPTIONS.map(option => (
              <button
                key={option.value}
                type="button"
                className={statusFilter === option.value ? 'is-selected' : ''}
                onClick={() => setStatusFilter(option.value)}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>

        {error && !formOpen && (
          <div role="alert" className="alert error">
            <span>{error}</span>
            <button type="button" className="alert-dismiss" onClick={() => setError('')} aria-label="Dismiss error">
              <CloseIcon />
            </button>
          </div>
        )}
        {notice && (
          <div role="status" className="alert success">
            <span>{notice}</span>
            <button type="button" className="alert-dismiss" onClick={() => setNotice('')} aria-label="Dismiss notice">
              <CloseIcon />
            </button>
          </div>
        )}

        {loading ? (
          <div className="empty-card">
            <p>Loading components…</p>
          </div>
        ) : components.length === 0 ? (
          <div className="empty-card">
            <div className="empty-icon"><ChipIcon /></div>
            <h3>{hasFilters ? 'No matching components' : 'No components registered yet'}</h3>
            <p>
              {hasFilters
                ? 'Adjust your search term, category selection, or status filter.'
                : activeCategories.length > 0
                  ? 'Add your first electronic component with specs, bin location, and reorder threshold.'
                  : 'Create a category first, then register your electronic components here.'}
            </p>
            {hasFilters ? (
              <button
                type="button"
                className="secondary"
                onClick={() => {
                  setSearch('');
                  setCategoryFilter('');
                  setStatusFilter('ALL');
                }}
              >
                Reset filters
              </button>
            ) : (
              activeCategories.length > 0 && (
                <button type="button" onClick={openCreateForm}>
                  <PlusIcon />
                  <span>Add component</span>
                </button>
              )
            )}
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Part Number</th>
                  <th>Component &amp; Specs</th>
                  <th>Category</th>
                  <th>Manufacturer / Package</th>
                  <th>Storage &amp; Threshold</th>
                  <th>Status</th>
                  <th className="actions-col">Actions</th>
                </tr>
              </thead>
              <tbody>
                {components.map(component => {
                  const specEntries = Object.entries(component.specifications || {});
                  return (
                    <tr key={component.id} className={!component.active ? 'row-inactive' : ''}>
                      <td>
                        <span className="code-pill">{component.partNumber}</span>
                      </td>
                      <td>
                        <div className="primary-cell">{component.name}</div>
                        {component.description && <div className="sub-cell">{component.description}</div>}
                        {specEntries.length > 0 && (
                          <div className="spec-chips">
                            {specEntries.map(([key, value]) => (
                              <span key={key} className="spec-chip">
                                <strong>{key}:</strong> {value}
                              </span>
                            ))}
                          </div>
                        )}
                      </td>
                      <td>
                        <span className="category-tag">
                          {component.categoryCode ? `${component.categoryCode} · ` : ''}
                          {component.categoryName}
                        </span>
                      </td>
                      <td>
                        <div className="primary-cell">{component.manufacturer || '—'}</div>
                        {component.packageType && <div className="sub-cell">{component.packageType}</div>}
                      </td>
                      <td>
                        <div className="location-pill">
                          <PinIcon />
                          <span>{component.storageLocation || '—'}</span>
                        </div>
                        <div className="threshold-line">
                          Min: <strong>{component.reorderThreshold ?? 0}</strong> {component.unit || 'pcs'}
                        </div>
                      </td>
                      <td>
                        <span className={`badge ${component.active ? 'badge-active' : 'badge-inactive'}`}>
                          {component.active ? 'Active' : 'Inactive'}
                        </span>
                      </td>
                      <td className="actions-col">
                        <div className="row-actions">
                          <button
                            type="button"
                            className="secondary small"
                            onClick={() => openEditForm(component)}
                            disabled={busyId === component.id}
                          >
                            Edit
                          </button>
                          <button
                            type="button"
                            className={`${component.active ? 'danger-soft' : 'secondary'} small`}
                            onClick={() => handleToggleActive(component)}
                            disabled={busyId === component.id}
                          >
                            {busyId === component.id
                              ? 'Updating…'
                              : component.active
                                ? 'Deactivate'
                                : 'Activate'}
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {formOpen && (
        <div
          className="modal-backdrop"
          onClick={event => {
            if (event.target === event.currentTarget && !submitting) {
              closeForm();
            }
          }}
        >
          <div className="modal-card" role="dialog" aria-modal="true" aria-labelledby="cmp-modal-title">
            <div className="modal-header">
              <div>
                <h3 id="cmp-modal-title">{editingId ? 'Edit Component' : 'New Component'}</h3>
                <p className="modal-subtitle">
                  Specify identity, storage bin, unit of measure, reorder threshold, and technical specs.
                </p>
              </div>
              <button
                type="button"
                className="ghost icon-btn"
                onClick={closeForm}
                disabled={submitting}
                aria-label="Close modal"
              >
                <CloseIcon />
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                {error && (
                  <div role="alert" className="alert error">
                    <span>{error}</span>
                  </div>
                )}

                <div>
                  <h4 className="form-section-title">Identity &amp; Classification</h4>
                  <div className="form-grid">
                    <label>
                      <span>Part number *</span>
                      <input
                        type="text"
                        required
                        maxLength={48}
                        value={form.partNumber}
                        onChange={event => setForm(prev => ({ ...prev, partNumber: event.target.value }))}
                        placeholder="e.g. RES-10K-0603"
                        autoFocus
                      />
                    </label>
                    <label>
                      <span>Name *</span>
                      <input
                        type="text"
                        required
                        maxLength={120}
                        value={form.name}
                        onChange={event => setForm(prev => ({ ...prev, name: event.target.value }))}
                        placeholder="e.g. 10kΩ 0603 SMD Resistor"
                      />
                    </label>
                    <label>
                      <span>Category *</span>
                      <select
                        required
                        value={form.categoryId}
                        onChange={event => setForm(prev => ({ ...prev, categoryId: event.target.value }))}
                      >
                        <option value="" disabled>
                          Select a category
                        </option>
                        {selectableCategories.map(category => (
                          <option key={category.id} value={category.id}>
                            {category.code} — {category.name}
                            {!category.active ? ' (Inactive)' : ''}
                          </option>
                        ))}
                      </select>
                    </label>
                    <label>
                      <span>Manufacturer</span>
                      <input
                        type="text"
                        maxLength={80}
                        value={form.manufacturer}
                        onChange={event => setForm(prev => ({ ...prev, manufacturer: event.target.value }))}
                        placeholder="e.g. Yageo"
                      />
                    </label>
                    <label>
                      <span>Package / Footprint</span>
                      <input
                        type="text"
                        maxLength={40}
                        value={form.packageType}
                        onChange={event => setForm(prev => ({ ...prev, packageType: event.target.value }))}
                        placeholder="e.g. 0603, DIP-8, TO-220"
                      />
                    </label>
                  </div>
                </div>

                <div>
                  <h4 className="form-section-title">Inventory &amp; Storage</h4>
                  <div className="form-grid">
                    <label>
                      <span>Unit of measure *</span>
                      <input
                        type="text"
                        required
                        maxLength={24}
                        list="unit-suggestions"
                        value={form.unit}
                        onChange={event => setForm(prev => ({ ...prev, unit: event.target.value }))}
                        placeholder="e.g. pcs, reels, tubes"
                      />
                      <datalist id="unit-suggestions">
                        {UNIT_SUGGESTIONS.map(unitOption => (
                          <option key={unitOption} value={unitOption} />
                        ))}
                      </datalist>
                    </label>
                    <label>
                      <span>Storage location *</span>
                      <input
                        type="text"
                        required
                        maxLength={80}
                        value={form.storageLocation}
                        onChange={event => setForm(prev => ({ ...prev, storageLocation: event.target.value }))}
                        placeholder="e.g. Rack A - Shelf 2 - Bin 04"
                      />
                    </label>
                    <label>
                      <span>Reorder threshold ({form.unit.trim() || 'units'}) *</span>
                      <input
                        type="number"
                        required
                        min={0}
                        max={1000000}
                        step={1}
                        value={form.reorderThreshold}
                        onChange={event => setForm(prev => ({ ...prev, reorderThreshold: event.target.value }))}
                        placeholder="e.g. 50"
                      />
                    </label>
                  </div>
                </div>

                <div className="spec-section">
                  <div className="spec-header">
                    <span>Technical specifications</span>
                    <button
                      type="button"
                      className="secondary small"
                      onClick={addSpecRow}
                      disabled={form.specs.length >= 25}
                    >
                      <PlusIcon />
                      <span>Add spec</span>
                    </button>
                  </div>
                  <div className="spec-rows">
                    {form.specs.map((row, index) => (
                      <div key={index} className="spec-row">
                        <input
                          type="text"
                          maxLength={40}
                          value={row.key}
                          onChange={event => updateSpecRow(index, 'key', event.target.value)}
                          placeholder="Parameter (e.g. Resistance)"
                          aria-label={`Specification ${index + 1} name`}
                        />
                        <input
                          type="text"
                          maxLength={120}
                          value={row.value}
                          onChange={event => updateSpecRow(index, 'value', event.target.value)}
                          placeholder="Value (e.g. 10 kΩ ±1%)"
                          aria-label={`Specification ${index + 1} value`}
                        />
                        <button
                          type="button"
                          className="ghost small"
                          onClick={() => removeSpecRow(index)}
                          aria-label={`Remove specification ${index + 1}`}
                        >
                          Remove
                        </button>
                      </div>
                    ))}
                  </div>
                </div>

                <div className="form-grid">
                  <label className="full-span">
                    <span>Description</span>
                    <textarea
                      rows={2}
                      maxLength={500}
                      value={form.description}
                      onChange={event => setForm(prev => ({ ...prev, description: event.target.value }))}
                      placeholder="Optional summary or handling notes"
                    />
                  </label>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="secondary" onClick={closeForm} disabled={submitting}>
                  Cancel
                </button>
                <button type="submit" disabled={submitting}>
                  {submitting ? 'Saving…' : editingId ? 'Save changes' : 'Create component'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </section>
  );
}
