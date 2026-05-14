// ============================================================
// ERP System — Main App Router + Mount
// ============================================================

function ERPApp() {
  const [route, setRoute] = useState('/');

  const navigate = useCallback((path) => {
    if (path === '/login') {
      window.location.href = 'Registration.html';
      return;
    }
    setRoute(path);
    window.scrollTo(0, 0);
  }, []);

  const renderPage = () => {
    switch (route) {
      case '/': return <DashboardPage/>;
      case '/sales/orders': return <SalesOrdersPage onNavigate={navigate}/>;
      case '/sales/invoices': return <SalesInvoicesPage/>;
      case '/sales/payments': return <PaymentsPage/>;
      case '/sales/returns': return <SalesReturnsPage/>;
      case '/purchase/orders': return <PurchaseOrdersPage/>;
      case '/purchase/receipts': return <GoodsReceiptsPage/>;
      case '/purchase/bills': return <SupplierBillsPage/>;
      case '/inventory/stock': return <StockLevelsPage/>;
      case '/inventory/movements': return <StockMovementsPage/>;
      case '/inventory/counts': return <InventoryCountsPage/>;
      case '/inventory/alerts': return <LowStockAlertsPage onNavigate={navigate}/>;
      case '/reports/sales': return <SalesSummaryPage/>;
      case '/admin/users': return <UserManagementPage/>;
      case '/admin/roles': return <RoleManagementPage/>;
      case '/admin/config': return <SystemConfigPage/>;
      case '/audit/logs': return <AuditLogPage/>;
      default: return <ErrorPage code={404} onNavigate={navigate}/>;
    }
  };

  return (
    <AppLayout route={route} onNavigate={navigate}>
      {renderPage()}
    </AppLayout>
  );
}

const erpRoot = ReactDOM.createRoot(document.getElementById('root'));
erpRoot.render(<ERPApp/>);
